package io.github.lmliam.kotventure.coroutines.time

import io.github.lmliam.kotventure.core.audience.message
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.ticks
import io.github.lmliam.kotventure.coroutines.event.RecordingAudience
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.github.lmliam.kotventure.test.time.ManualTicker
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldEndWith
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.matchers.types.shouldBeSameInstanceAs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private fun tickerScope(ticker: Ticker): CoroutineScope =
        CoroutineScope(SupervisorJob() + ticker.asCoroutineDispatcher())

class TickerDispatcherTest :
    StringSpec(
        {
            "a launched body waits for the ticker" {
                val ticker = ManualTicker()
                val log = mutableListOf<String>()

                tickerScope(ticker).launch { log += "ran" }

                log.shouldBeEmpty()

                ticker.advance(1.ticks)

                log shouldBe listOf("ran")
            }

            "a delay resumes after exactly its interval" {
                val ticker = ManualTicker()
                val log = mutableListOf<String>()

                tickerScope(ticker).launch {
                    log += "start"
                    delay(1.seconds)
                    log += "end"
                }

                ticker.advance(1.ticks)
                log shouldBe listOf("start")

                ticker.advance(18.ticks)
                log shouldBe listOf("start")

                ticker.advance(1.ticks)
                log shouldBe listOf("start", "end")
            }

            "a tick delay reaches the ticker unchanged and costs no extra tick" {
                val ticker = RecordingTicker(ManualTicker())
                val log = mutableListOf<String>()

                tickerScope(ticker).launch {
                    delay(5.ticks)
                    log += "end"
                }

                ticker.advance(1.ticks)
                log.shouldBeEmpty()

                ticker.advance(4.ticks)
                log shouldBe listOf("end")

                ticker.delays shouldBe listOf(Duration.ZERO, 5.ticks)
            }

            "concurrent delays resume in due-time order" {
                val ticker = ManualTicker()
                val log = mutableListOf<String>()
                val scope = tickerScope(ticker)

                scope.launch {
                    delay(2.seconds)
                    log += "slow"
                }
                scope.launch {
                    delay(1.seconds)
                    log += "fast"
                }

                ticker.advance(3.seconds)

                log shouldBe listOf("fast", "slow")
            }

            "withTimeout fails at the deadline of the ticker" {
                val ticker = ManualTicker()
                var failure: Throwable? = null

                tickerScope(ticker).launch {
                    runCatching { withTimeout(1.seconds) { delay(10.seconds) } }
                        .onFailure { failure = it }
                }

                ticker.advance(2.seconds)

                failure.shouldBeInstanceOf<TimeoutCancellationException>()
            }

            "withTimeoutOrNull gives null after the deadline" {
                val ticker = ManualTicker()
                var result: String? = "unset"

                tickerScope(ticker).launch {
                    result =
                        withTimeoutOrNull(1.seconds) {
                            delay(10.seconds)
                            "done"
                        }
                }

                ticker.advance(2.seconds)

                result shouldBe null
            }

            "withTimeoutOrNull gives the value when the body completes first" {
                val ticker = ManualTicker()
                var result: String? = null

                tickerScope(ticker).launch {
                    result =
                        withTimeoutOrNull(10.seconds) {
                            delay(1.seconds)
                            "done"
                        }
                }

                ticker.advance(2.seconds)

                result shouldBe "done"
            }

            "cancelling a job cancels the pending ticker task" {
                val ticker = RecordingTicker(ManualTicker())
                val log = mutableListOf<String>()

                val job =
                    tickerScope(ticker).launch {
                        delay(10.seconds)
                        log += "end"
                    }

                ticker.advance(1.ticks)
                job.cancel()

                ticker.cancellations shouldBe 1

                ticker.advance(20.seconds)

                log.shouldBeEmpty()
            }

            "an immediate dispatch continues in place when the ticker owns the thread" {
                val ticker = ManualTicker()
                val immediate = ticker.asCoroutineDispatcher().immediate
                val log = mutableListOf<String>()

                ticker.once {
                    log += "before"
                    CoroutineScope(immediate).launch { log += "inside" }
                    log += "after"
                }
                ticker.advance(1.ticks)

                log shouldBe listOf("before", "inside", "after")
            }

            "an immediate dispatch waits when the ticker does not own the thread" {
                val ticker = ManualTicker()
                val log = mutableListOf<String>()

                CoroutineScope(ticker.asCoroutineDispatcher().immediate).launch { log += "ran" }

                log.shouldBeEmpty()

                ticker.advance(1.ticks)

                log shouldBe listOf("ran")
            }

            "an immediate delay stays on the ticker" {
                val ticker = ManualTicker()
                val log = mutableListOf<String>()

                CoroutineScope(ticker.asCoroutineDispatcher().immediate).launch {
                    delay(1.seconds)
                    log += "end"
                }

                ticker.advance(19.ticks)
                log.shouldBeEmpty()

                ticker.advance(1.ticks)
                log shouldBe listOf("end")
            }

            "the immediate variant of an immediate dispatcher is itself" {
                val immediate = ManualTicker().asCoroutineDispatcher().immediate

                immediate.immediate shouldBeSameInstanceAs immediate
            }

            "a delayed coroutine sends through the audience DSL" {
                val ticker = ManualTicker()
                val audience = RecordingAudience()

                tickerScope(ticker).launch {
                    delay(1.seconds)
                    audience.message { text("Respawn ready") }
                }

                ticker.advance(2.seconds)

                audience.messages.single() shouldHaveContent "Respawn ready"
            }

            "each dispatcher names its ticker" {
                val ticker = ManualTicker()
                val dispatcher = ticker.asCoroutineDispatcher()

                "$dispatcher" shouldContain "$ticker"
                "${dispatcher.immediate}" shouldEndWith ".immediate"
            }
        },
    )
