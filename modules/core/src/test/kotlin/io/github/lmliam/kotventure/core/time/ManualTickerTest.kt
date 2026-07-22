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

                ticker.every(100.milliseconds) { count++ }

                ticker.advance(250.milliseconds)

                count shouldBe 2
            }

            "preserves phase when advancing past multiple intervals" {
                val ticker = ManualTicker()
                val fires = mutableListOf<Int>()
                var n = 0

                ticker.every(1.seconds) {
                    n++
                    fires += n
                }

                ticker.advance(3.seconds)

                fires shouldBe listOf(1, 2, 3)
            }

            "cancel stops further invocations" {
                val ticker = ManualTicker()
                var count = 0

                val task = ticker.every(1.seconds) { count++ }
                ticker.advance(1.seconds)
                task.cancel()
                ticker.advance(5.seconds)

                count shouldBe 1
            }

            "cancel is idempotent" {
                val ticker = ManualTicker()
                val task = ticker.every(1.seconds) { }
                task.cancel()
                task.cancel()
            }

            "task that cancels itself does not reschedule" {
                val ticker = ManualTicker()
                var count = 0
                lateinit var task: TickerTask

                task =
                    ticker.every(1.seconds) {
                        count++
                        task.cancel()
                    }

                ticker.advance(5.seconds)

                count shouldBe 1
            }

            "rejects non-positive interval" {
                val ticker = ManualTicker()
                shouldThrow<IllegalArgumentException> {
                    ticker.every(0.seconds) { }
                }
                shouldThrow<IllegalArgumentException> {
                    ticker.every((-1).seconds) { }
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
                ticker.every(1.seconds) { count++ }
                ticker.advance(0.seconds)
                count shouldBe 0
            }

            "fires a one-shot task exactly once after its delay" {
                val ticker = ManualTicker()
                var count = 0

                ticker.after(1.seconds) { count++ }

                ticker.advance(500.milliseconds)
                count shouldBe 0

                ticker.advance(10.seconds)
                count shouldBe 1
            }

            "a zero delay is due at the current time and runs on the next advance" {
                val ticker = ManualTicker()
                var count = 0

                ticker.after { count++ }
                count shouldBe 0

                ticker.advance(1.ticks)

                count shouldBe 1
            }

            "cancel prevents a one-shot run" {
                val ticker = ManualTicker()
                var count = 0

                ticker.after(1.seconds) { count++ }.cancel()
                ticker.advance(5.seconds)

                count shouldBe 0
            }

            "cancel on a one-shot task is idempotent" {
                val ticker = ManualTicker()
                val task = ticker.after(1.seconds) { }
                task.cancel()
                task.cancel()
            }

            "rejects a negative once delay" {
                val ticker = ManualTicker()
                shouldThrow<IllegalArgumentException> {
                    ticker.after((-1).seconds) { }
                }
            }

            "runs one-shot and repeating tasks in due-time then registration order" {
                val ticker = ManualTicker()
                val order = mutableListOf<String>()

                ticker.every(1.seconds) { order += "repeating" }
                ticker.after(1.seconds) { order += "once at one" }
                ticker.after(500.milliseconds) { order += "once at half" }

                ticker.advance(2.seconds)

                order shouldBe listOf("once at half", "repeating", "once at one", "repeating")
            }

            "work scheduled during an advance runs in the same advance when it is due" {
                val ticker = ManualTicker()
                val order = mutableListOf<String>()

                ticker.after(1.seconds) {
                    order += "outer"
                    ticker.after { order += "inner" }
                }

                ticker.advance(2.seconds)

                order shouldBe listOf("outer", "inner")
            }

            "isCurrent is false outside advance" {
                ManualTicker().isCurrent shouldBe false
            }

            "isCurrent is true inside a scheduled action" {
                val ticker = ManualTicker()
                var owned = false

                ticker.after(1.seconds) { owned = ticker.isCurrent }
                ticker.advance(1.seconds)

                owned shouldBe true
            }

            "isCurrent returns to false after an action throws" {
                val ticker = ManualTicker()
                ticker.after(1.seconds) { error("boom") }

                shouldThrow<IllegalStateException> { ticker.advance(1.seconds) }

                ticker.isCurrent shouldBe false
            }
        },
    )
