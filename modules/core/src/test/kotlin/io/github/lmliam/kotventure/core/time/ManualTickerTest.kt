package io.github.lmliam.kotventure.core.time

import io.github.lmliam.kotventure.test.time.ManualTicker
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

class ManualTickerTest :
    StringSpec(
        {
            "fires a repeating task after each interval" {
                val ticker = ManualTicker()
                var count = 0

                ticker.repeating(100.milliseconds) { count++ }

                ticker.advance(250.milliseconds)

                count shouldBe 2
            }

            "preserves phase when advancing past multiple intervals" {
                val ticker = ManualTicker()
                val fires = mutableListOf<Int>()
                var n = 0

                ticker.repeating(1.seconds) {
                    n++
                    fires += n
                }

                ticker.advance(3.seconds)

                fires shouldBe listOf(1, 2, 3)
            }

            "cancel stops further invocations" {
                val ticker = ManualTicker()
                var count = 0

                val task = ticker.repeating(1.seconds) { count++ }
                ticker.advance(1.seconds)
                task.cancel()
                ticker.advance(5.seconds)

                count shouldBe 1
            }

            "cancel is idempotent" {
                val ticker = ManualTicker()
                val task = ticker.repeating(1.seconds) { }
                task.cancel()
                task.cancel()
            }

            "task that cancels itself does not reschedule" {
                val ticker = ManualTicker()
                var count = 0
                lateinit var task: TickerTask

                task =
                    ticker.repeating(1.seconds) {
                        count++
                        task.cancel()
                    }

                ticker.advance(5.seconds)

                count shouldBe 1
            }

            "rejects non-positive interval" {
                val ticker = ManualTicker()
                shouldThrow<IllegalArgumentException> {
                    ticker.repeating(0.seconds) { }
                }
                shouldThrow<IllegalArgumentException> {
                    ticker.repeating((-1).seconds) { }
                }
            }

            "rejects negative advance" {
                val ticker = ManualTicker()
                shouldThrow<IllegalArgumentException> {
                    ticker.advance((-1).milliseconds)
                }
            }

            "zero advance is a no-op" {
                val ticker = ManualTicker()
                var count = 0
                ticker.repeating(1.seconds) { count++ }
                ticker.advance(0.seconds)
                count shouldBe 0
            }
        },
    )
