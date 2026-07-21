package io.github.lmliam.kotventure.coroutines.prompt

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.coroutines.event.RecordingAudience
import io.github.lmliam.kotventure.coroutines.event.RecordingClickCallbackProvider
import io.github.lmliam.kotventure.test.text.shouldHaveClickEvent
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration.Companion.minutes
import java.time.Duration as JavaDuration

@OptIn(ExperimentalCoroutinesApi::class)
class AskDslTest :
    StringSpec(
        {
            "resumes with the clicked option's value" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val audience = RecordingAudience()

                    val answer =
                        async {
                            audience.ask {
                                text("Choose a kit: ")
                                option(Kit.ARCHER) { text(Kit.ARCHER.label) }
                                option(Kit.MAGE) { text(Kit.MAGE.label) }
                            }
                        }
                    advanceUntilIdle()

                    RecordingClickCallbackProvider.fire(1, audience)

                    answer.await() shouldBe Kit.MAGE
                }
            }

            "sends one message with a clickable child for each option" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val audience = RecordingAudience()

                    val answer =
                        async {
                            audience.ask {
                                option(Kit.ARCHER) { text(Kit.ARCHER.label) }
                                option(Kit.MAGE) { text(Kit.MAGE.label) }
                            }
                        }
                    advanceUntilIdle()

                    val message = audience.messages.single()
                    message.children() shouldHaveSize 2
                    message.children()[0] shouldHaveClickEvent RecordingClickCallbackProvider.eventAt(0)
                    message.children()[0].children().single() shouldHaveContent Kit.ARCHER.label
                    message.children()[1] shouldHaveClickEvent RecordingClickCallbackProvider.eventAt(1)
                    message.children()[1].children().single() shouldHaveContent Kit.MAGE.label

                    RecordingClickCallbackProvider.fire(0, audience)
                    answer.await() shouldBe Kit.ARCHER
                }
            }

            "keeps the first answer when a second option is clicked" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val audience = RecordingAudience()

                    val answer =
                        async {
                            audience.ask {
                                option(Kit.ARCHER) { text(Kit.ARCHER.label) }
                                option(Kit.MAGE) { text(Kit.MAGE.label) }
                            }
                        }
                    advanceUntilIdle()

                    RecordingClickCallbackProvider.fire(0, audience)
                    shouldNotThrowAny {
                        RecordingClickCallbackProvider.fire(1, audience)
                        RecordingClickCallbackProvider.fire(0, audience)
                    }

                    answer.await() shouldBe Kit.ARCHER
                }
            }

            "a cancelled prompt resumes with nothing and ignores later clicks" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val audience = RecordingAudience()

                    val answer =
                        async {
                            withTimeoutOrNull(1.minutes) {
                                audience.ask { option(Kit.ARCHER) { text(Kit.ARCHER.label) } }
                            }
                        }
                    advanceUntilIdle()

                    answer.await() shouldBe null
                    shouldNotThrowAny { RecordingClickCallbackProvider.fire(0, audience) }
                }
            }

            "rejects a prompt without options and sends nothing" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val audience = RecordingAudience()

                    shouldThrow<IllegalStateException> {
                        audience.ask<Kit> { text("Choose a kit: ") }
                    }

                    audience.messages.shouldBeEmpty()
                }
            }

            "rejects a click event applied inside an option label" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val audience = RecordingAudience()

                    shouldThrow<IllegalStateException> {
                        audience.ask<Kit> {
                            option(Kit.ARCHER) {
                                click { run("/kit archer") }
                                text(Kit.ARCHER.label)
                            }
                        }
                    }

                    audience.messages.shouldBeEmpty()
                }
            }

            "forwards the lifetime into every option callback" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val audience = RecordingAudience()

                    val answer =
                        async {
                            audience.ask(lifetime = 10.minutes) {
                                option(Kit.ARCHER) { text(Kit.ARCHER.label) }
                                option(Kit.MAGE) { text(Kit.MAGE.label) }
                            }
                        }
                    advanceUntilIdle()

                    RecordingClickCallbackProvider.recordedCount() shouldBe 2
                    RecordingClickCallbackProvider.optionsAt(0).lifetime() shouldBe JavaDuration.ofMinutes(10)
                    RecordingClickCallbackProvider.optionsAt(1).lifetime() shouldBe JavaDuration.ofMinutes(10)

                    RecordingClickCallbackProvider.fire(0, audience)
                    answer.await() shouldBe Kit.ARCHER
                }
            }

            "gives the asked audience as the viewer, inside and outside an option" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val audience = RecordingAudience()
                    lateinit var outside: Any
                    lateinit var inside: Any

                    val answer =
                        async {
                            audience.ask<Kit> {
                                outside = viewer
                                option(Kit.ARCHER) {
                                    inside = this@ask.viewer
                                    text(Kit.ARCHER.label)
                                }
                            }
                        }
                    advanceUntilIdle()

                    outside shouldBeSameInstanceAs audience
                    inside shouldBeSameInstanceAs audience

                    RecordingClickCallbackProvider.fire(0, audience)
                    answer.await() shouldBe Kit.ARCHER
                }
            }

            "renders a prompt value again for each audience it asks" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val first = RecordingAudience()
                    val second = RecordingAudience()
                    val viewers = mutableListOf<Any>()
                    val prompt =
                        Prompt<Kit> {
                            viewers += viewer
                            option(Kit.ARCHER) { text(Kit.ARCHER.label) }
                            option(Kit.MAGE) { text(Kit.MAGE.label) }
                        }

                    val firstAnswer = async { first.ask(prompt) }
                    advanceUntilIdle()
                    val secondAnswer = async { second.ask(prompt) }
                    advanceUntilIdle()

                    RecordingClickCallbackProvider.fire(3, second)
                    RecordingClickCallbackProvider.fire(0, first)

                    firstAnswer.await() shouldBe Kit.ARCHER
                    secondAnswer.await() shouldBe Kit.MAGE
                    viewers shouldBe listOf(first, second)
                }
            }

            "asks a prompt declared as an object" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val audience = RecordingAudience()

                    val answer = async { audience.ask(KitPrompt) }
                    advanceUntilIdle()

                    RecordingClickCallbackProvider.fire(0, audience)

                    answer.await() shouldBe Kit.ARCHER
                }
            }

            "asks a prompt class that carries its own dependencies" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val audience = RecordingAudience()

                    val answer = async { audience.ask(UnlockedKitPrompt(listOf(Kit.MAGE, Kit.KNIGHT))) }
                    advanceUntilIdle()

                    RecordingClickCallbackProvider.recordedCount() shouldBe 2
                    RecordingClickCallbackProvider.fire(1, audience)

                    answer.await() shouldBe Kit.KNIGHT
                }
            }
        },
    )
