package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.bossbar.shouldHaveProgress
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.time.ManualTicker
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.kyori.adventure.bossbar.BossBar
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TimedBossBarHooksTest :
    StringSpec(
        {
            "dynamic name re-renders each tick" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, audience, 3.seconds) {
                        name { remaining -> text("T-${remaining.inWholeSeconds}") }
                        every(1.seconds)
                    }

                timed.bar.name() shouldContainText "T-3"

                ticker.advance(1.seconds)
                timed.bar.name() shouldContainText "T-2"

                ticker.advance(1.seconds)
                timed.bar.name() shouldContainText "T-1"

                ticker.advance(1.seconds)
                timed.bar.name() shouldContainText "T-0"
            }

            "dynamic name block is a full component scope" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, audience, 2.seconds) {
                        name { remaining ->
                            text("Ends in ")
                            text("${remaining.inWholeSeconds}s") { bold() }
                        }
                        every(1.seconds)
                    }

                timed.bar.name() shouldContainText "Ends in "
                timed.bar.name() shouldContainText "2s"

                ticker.advance(1.seconds)

                timed.bar.name() shouldContainText "1s"
            }

            "updates progress and name before onTick and finishes once" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()
                val events = mutableListOf<String>()
                val observedProgress = mutableListOf<Float>()

                val timed =
                    timedBossBar(ticker, audience, 2.seconds) {
                        name { remaining ->
                            events += "name:${remaining.inWholeSeconds}"
                            text("n")
                        }
                        every(1.seconds)
                        onTick { remaining ->
                            events += "tick:${remaining.inWholeSeconds}"
                            observedProgress += bar.progress()
                        }
                        onFinish { events += "finish" }
                        onCancel { events += "cancel" }
                    }

                events.clear()

                ticker.advance(2.seconds)

                events shouldContainExactly
                    listOf(
                        "name:1",
                        "tick:1",
                        "name:0",
                        "tick:0",
                        "finish",
                    )
                observedProgress shouldBe listOf(0.5f, 0f)
                timed.bar shouldHaveProgress BossBar.MIN_PROGRESS
            }

            "onCancel fires once and never with onFinish" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()
                var finishes = 0
                var cancels = 0

                val timed =
                    timedBossBar(ticker, audience, 5.seconds) {
                        name { text("X") }
                        every(1.seconds)
                        onFinish { finishes++ }
                        onCancel { cancels++ }
                    }

                ticker.advance(1.seconds)
                timed.cancel()
                ticker.advance(10.seconds)

                finishes shouldBe 0
                cancels shouldBe 1
            }

            "never re-pushes a fixed name" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()
                val nameChanges = BossBarNameChangeRecorder()

                val timed =
                    timedBossBar(ticker, audience, 3.seconds) {
                        name { text("Fixed") }
                        every(1.seconds)
                    }

                timed.bar.addListener(nameChanges)

                ticker.advance(3.seconds)

                nameChanges.names shouldHaveSize 0
            }

            "pushes a dynamic name only when the rendered component changes" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()
                val nameChanges = BossBarNameChangeRecorder()

                val timed =
                    timedBossBar(ticker, audience, 4.seconds) {
                        name { remaining -> text("${(remaining.inWholeSeconds + 1) / 2}") }
                        every(1.seconds)
                    }

                timed.bar.addListener(nameChanges)

                ticker.advance(4.seconds)

                nameChanges.names shouldHaveSize 2
            }

            "cancel from the final onTick does not override onFinish" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()
                var finishes = 0
                var cancels = 0

                val timed =
                    timedBossBar(ticker, audience, 1.seconds) {
                        name { text("X") }
                        every(1.seconds)
                        onTick { remaining ->
                            if (remaining == Duration.ZERO) {
                                cancel()
                            }
                        }
                        onFinish { finishes++ }
                        onCancel { cancels++ }
                    }

                ticker.advance(1.seconds)

                finishes shouldBe 1
                cancels shouldBe 0
                timed.isRunning shouldBe false
                audience.hidden shouldContainExactly listOf(timed.bar)
            }

            "exception from final onTick hides viewers, finishes, and rethrows" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()
                var finishes = 0
                val boom = IllegalStateException("tick failed")

                val timed =
                    timedBossBar(ticker, audience, 1.seconds) {
                        name { text("X") }
                        every(1.seconds)
                        onTick { remaining ->
                            if (remaining == Duration.ZERO) {
                                throw boom
                            }
                        }
                        onFinish { finishes++ }
                    }

                shouldThrow<IllegalStateException> {
                    ticker.advance(1.seconds)
                } shouldBe boom

                finishes shouldBe 1
                timed.isRunning shouldBe false
                timed.remaining shouldBe Duration.ZERO
                audience.hidden shouldContainExactly listOf(timed.bar)
            }
        },
    )
