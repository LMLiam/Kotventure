package io.github.lmliam.kotventure.coroutines.event

import io.github.lmliam.kotventure.core.event.click
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.shouldHaveClickEvent
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.kotest.assertions.throwables.shouldNotThrowAny
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
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration.Companion.minutes
import java.time.Duration as JavaDuration

@OptIn(ExperimentalCoroutinesApi::class)
class CallbackDslTest :
    StringSpec(
        {
            "runs the suspend body with the clicking audience after advancing" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
                    val clicker = RecordingAudience()

                    val event =
                        click {
                            callback(scope) { audience -> audience.sendMessage(text("Reward claimed")) }
                        }
                    text("Claim").clickEvent(event) shouldHaveClickEvent RecordingClickCallbackProvider.lastEvent

                    RecordingClickCallbackProvider.fire(clicker)
                    advanceUntilIdle()

                    clicker.messages shouldHaveSize 1
                    clicker.messages.single() shouldHaveContent "Reward claimed"
                }
            }

            "forwards uses and lifetime into the recorded options" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))

                    click {
                        callback(scope, uses = 3, lifetime = 10.minutes) { }
                    }

                    RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 3
                    RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe JavaDuration.ofMinutes(10)
                }
            }

            "forwards prebuilt options into the recorded options" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
                    val options =
                        ClickCallback.Options
                                .builder()
                                .uses(4)
                                .lifetime(JavaDuration.ofSeconds(45))
                                .build()

                    click {
                        callback(scope, options) { }
                    }

                    RecordingClickCallbackProvider.lastOptions shouldBe options
                }
            }

            "runs nothing when the scope is cancelled before the click" {
                runTest {
                    RecordingClickCallbackProvider.reset()
                    val scope = CoroutineScope(StandardTestDispatcher(testScheduler))
                    val clicker = RecordingAudience()

                    click {
                        callback(scope) { audience -> audience.sendMessage(text("Reward claimed")) }
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

                    click {
                        callback(scope) { error("boom") }
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
