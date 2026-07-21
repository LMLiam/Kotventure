package io.github.lmliam.kotventure.coroutines.event

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.shouldHaveClickEvent
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import net.kyori.adventure.text.Component
import kotlin.time.Duration.Companion.minutes
import java.time.Duration as JavaDuration

private val rewardMessage: Component = text("Reward claimed")

private fun CoroutineScope.contextClickComponent(): Component =
    component {
        text("Claim") {
            click { clicker -> clicker.sendMessage(rewardMessage) }
        }
    }

private fun CoroutineScope.contextOptionsComponent(): Component =
    component {
        text("Claim") {
            click(
                options = {
                    uses(3)
                    lifetime(10.minutes)
                },
            ) { clicker -> clicker.sendMessage(rewardMessage) }
        }
    }

@OptIn(ExperimentalCoroutinesApi::class)
class ClickDslTest :
    StringSpec(
        {
            "attached click runs the suspend body with the clicking audience" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
                    val clicker = RecordingAudience()

                    val message =
                        component {
                            text("Claim") {
                                click(scope) { audience -> audience.sendMessage(rewardMessage) }
                            }
                        }
                    message.children().single() shouldHaveClickEvent RecordingClickCallbackProvider.lastEvent

                    RecordingClickCallbackProvider.fire(clicker)
                    advanceUntilIdle()

                    clicker.messages shouldHaveSize 1
                    clicker.messages.single() shouldHaveContent "Reward claimed"
                }
            }

            "context click attaches and runs inside a coroutine scope receiver" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
                    val clicker = RecordingAudience()

                    val message = scope.contextClickComponent()
                    message.children().single() shouldHaveClickEvent RecordingClickCallbackProvider.lastEvent

                    RecordingClickCallbackProvider.fire(clicker)
                    advanceUntilIdle()

                    clicker.messages shouldHaveSize 1
                    clicker.messages.single() shouldHaveContent "Reward claimed"
                }
            }

            "attached click forwards the options block into the recorded options" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))

                    component {
                        text("Claim") {
                            click(
                                scope,
                                options = {
                                    uses(3)
                                    lifetime(10.minutes)
                                },
                            ) { }
                        }
                    }

                    RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 3
                    RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe JavaDuration.ofMinutes(10)
                }
            }

            "context click forwards the options block into the recorded options" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))

                    scope.contextOptionsComponent()

                    RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 3
                    RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe JavaDuration.ofMinutes(10)
                }
            }

            "reusable click builds an event that attaches to many components" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
                    val clicker = RecordingAudience()

                    val claim = click(scope) { audience -> audience.sendMessage(rewardMessage) }

                    text("[Claim]").clickEvent(claim) shouldHaveClickEvent RecordingClickCallbackProvider.lastEvent
                    text("[Again]").clickEvent(claim) shouldHaveClickEvent RecordingClickCallbackProvider.lastEvent

                    RecordingClickCallbackProvider.fire(clicker)
                    advanceUntilIdle()

                    clicker.messages shouldHaveSize 1
                    clicker.messages.single() shouldHaveContent "Reward claimed"
                }
            }

            "reusable click forwards the options block into the recorded options" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))

                    click(
                        scope,
                        options = {
                            uses(3)
                            lifetime(10.minutes)
                        },
                    ) { }

                    RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 3
                    RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe JavaDuration.ofMinutes(10)
                }
            }

            "rejects a second use count in one options block" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))

                    shouldThrow<IllegalStateException> {
                        click(
                            scope,
                            options = {
                                uses(1)
                                uses(2)
                            },
                        ) { }
                    }
                }
            }

            "runs nothing when the scope is cancelled before the click" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
                    val clicker = RecordingAudience()

                    component {
                        text("Claim") {
                            click(scope) { audience -> audience.sendMessage(rewardMessage) }
                        }
                    }
                    scope.cancel()

                    RecordingClickCallbackProvider.fire(clicker)
                    advanceUntilIdle()

                    clicker.messages.shouldBeEmpty()
                }
            }

            "sends a failing body to the scope handler, not the click thread" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val handled = CompletableDeferred<Throwable>()
                    val handler = CoroutineExceptionHandler { _, throwable -> handled.complete(throwable) }
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler) + handler)

                    component {
                        text("Claim") {
                            click(scope) { error("boom") }
                        }
                    }

                    shouldNotThrowAny {
                        RecordingClickCallbackProvider.fire(RecordingAudience())
                    }
                    advanceUntilIdle()

                    handled.await().message shouldBe "boom"
                }
            }
        },
    )
