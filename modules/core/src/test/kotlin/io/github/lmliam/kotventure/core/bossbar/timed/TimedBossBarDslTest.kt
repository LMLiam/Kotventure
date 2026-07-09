package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.audience.bossBar
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.ticks
import io.github.lmliam.kotventure.test.bossbar.shouldHaveColor
import io.github.lmliam.kotventure.test.bossbar.shouldHaveOverlay
import io.github.lmliam.kotventure.test.bossbar.shouldHaveProgress
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.time.ManualTicker
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.floats.shouldBeWithinPercentageOf
import io.kotest.matchers.shouldBe
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

private fun nameChangeListener(target: MutableList<Component>): BossBar.Listener =
    object : BossBar.Listener {
        override fun bossBarNameChanged(
            bar: BossBar,
            oldName: Component,
            newName: Component,
        ) {
            target += newName
        }
    }

private class TimedBossBarRecordingAudience : Audience {
    val shown = mutableListOf<BossBar>()
    val hidden = mutableListOf<BossBar>()

    override fun showBossBar(bar: BossBar) {
        shown += bar
    }

    override fun hideBossBar(bar: BossBar) {
        hidden += bar
    }
}

class TimedBossBarDslTest :
    StringSpec(
        {
            "defaults to a 1→0 countdown and lands exactly on to" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 10.seconds) {
                            name { text("Countdown") }
                            every(1.seconds)
                        }
                    }

                timed.bar shouldHaveProgress BossBar.MAX_PROGRESS
                timed.remaining shouldBe 10.seconds
                timed.isRunning shouldBe true

                ticker.advance(5.seconds)
                timed.remaining shouldBe 5.seconds
                timed.bar.progress().shouldBeWithinPercentageOf(0.5f, 0.01)

                ticker.advance(5.seconds)
                timed.remaining shouldBe Duration.ZERO
                timed.bar shouldHaveProgress BossBar.MIN_PROGRESS
                timed.isRunning shouldBe false
                audience.hidden shouldContainExactly listOf(timed.bar)
            }

            "interpolates arbitrary from→to progress" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 4.seconds) {
                            name { text("Fill") }
                            progress(from = 0.25f, to = 0.75f)
                            every(1.seconds)
                        }
                    }

                timed.bar shouldHaveProgress 0.25f
                ticker.advance(2.seconds)
                timed.bar.progress().shouldBeWithinPercentageOf(0.5f, 0.01)
                ticker.advance(2.seconds)
                timed.bar shouldHaveProgress 0.75f
            }

            "auto-hides from all tracked audiences on completion" {
                val ticker = ManualTicker()
                val creator = TimedBossBarRecordingAudience()
                val extra = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        creator.bossBar(over = 1.seconds) {
                            name { text("Raid") }
                            every(1.seconds)
                        }
                    }
                timed.show(extra)

                ticker.advance(1.seconds)

                creator.hidden shouldContainExactly listOf(timed.bar)
                extra.hidden shouldContainExactly listOf(timed.bar)
                timed.isRunning shouldBe false
            }

            "cancel hides immediately and is idempotent" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()
                var cancels = 0

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 10.seconds) {
                            name { text("Abort") }
                            every(1.seconds)
                            onCancel { cancels++ }
                        }
                    }

                ticker.advance(3.seconds)
                timed.cancel()
                timed.cancel()

                cancels shouldBe 1
                timed.isRunning shouldBe false
                timed.remaining shouldBe 7.seconds
                audience.hidden shouldContainExactly listOf(timed.bar)

                ticker.advance(10.seconds)
                timed.remaining shouldBe 7.seconds
            }

            "pause freezes remaining and stops ticking; resume continues" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 10.seconds) {
                            name { text("Hold") }
                            every(1.seconds)
                        }
                    }

                ticker.advance(3.seconds)
                timed.remaining shouldBe 7.seconds

                timed.pause()
                timed.isPaused shouldBe true
                ticker.advance(5.seconds)
                timed.remaining shouldBe 7.seconds
                // Countdown 1→0: after 3s of 10s, fill is 0.7 not 0.3.
                timed.bar.progress().shouldBeWithinPercentageOf(0.7f, 0.01)

                timed.resume()
                timed.isPaused shouldBe false
                ticker.advance(2.seconds)
                timed.remaining shouldBe 5.seconds
            }

            "dynamic name re-renders each tick" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 3.seconds) {
                            name { remaining -> text("T-${remaining.inWholeSeconds}") }
                            every(1.seconds)
                        }
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
                    context(ticker) {
                        audience.bossBar(over = 2.seconds) {
                            name { remaining ->
                                text("Ends in ")
                                text("${remaining.inWholeSeconds}s") { bold() }
                            }
                            every(1.seconds)
                        }
                    }

                timed.bar.name() shouldContainText "Ends in "
                timed.bar.name() shouldContainText "2s"
                ticker.advance(1.seconds)
                timed.bar.name() shouldContainText "1s"
            }

            "hook order is progress → name → onTick; onFinish fires once on natural completion" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()
                val events = mutableListOf<String>()

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 2.seconds) {
                            name { remaining ->
                                events += "name:${remaining.inWholeSeconds}"
                                text("n")
                            }
                            every(1.seconds)
                            onTick { remaining ->
                                events += "tick:${remaining.inWholeSeconds}:p=${bar.progress()}"
                            }
                            onFinish {
                                events += "finish"
                            }
                            onCancel {
                                events += "cancel"
                            }
                        }
                    }

                // Initial dynamic name render at construction (not a tick).
                events.clear()

                ticker.advance(2.seconds)

                events[0] shouldBe "name:1"
                events[1].startsWith("tick:1:p=") shouldBe true
                events[2] shouldBe "name:0"
                events[3].startsWith("tick:0:p=") shouldBe true
                events[4] shouldBe "finish"
                events shouldHaveSize 5
                timed.bar shouldHaveProgress BossBar.MIN_PROGRESS
            }

            "onCancel fires once and never with onFinish" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()
                var finishes = 0
                var cancels = 0

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 5.seconds) {
                            name { text("X") }
                            every(1.seconds)
                            onFinish { finishes++ }
                            onCancel { cancels++ }
                        }
                    }

                ticker.advance(1.seconds)
                timed.cancel()
                ticker.advance(10.seconds)

                finishes shouldBe 0
                cancels shouldBe 1
            }

            "configures colour and overlay like a static bar" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 1.seconds) {
                            name { text("Styled") }
                            color(red)
                            overlay(notched10)
                        }
                    }

                timed.bar shouldHaveColor BossBar.Color.RED
                timed.bar shouldHaveOverlay BossBar.Overlay.NOTCHED_10
                audience.shown shouldContainExactly listOf(timed.bar)
            }

            "rejects non-positive over" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                shouldThrow<IllegalArgumentException> {
                    context(ticker) {
                        audience.bossBar(over = Duration.ZERO) {
                            name { text("Bad") }
                        }
                    }
                }
                shouldThrow<IllegalArgumentException> {
                    context(ticker) {
                        audience.bossBar(over = (-1).seconds) {
                            name { text("Bad") }
                        }
                    }
                }
            }

            "rejects out-of-range progress endpoints" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                shouldThrow<IllegalArgumentException> {
                    context(ticker) {
                        audience.bossBar(over = 1.seconds) {
                            name { text("Bad") }
                            progress(from = 1.5f, to = 0f)
                        }
                    }
                }
                shouldThrow<IllegalArgumentException> {
                    context(ticker) {
                        audience.bossBar(over = 1.seconds) {
                            name { text("Bad") }
                            progress(from = 0f, to = -0.1f)
                        }
                    }
                }
            }

            "rejects non-positive every" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                shouldThrow<IllegalArgumentException> {
                    context(ticker) {
                        audience.bossBar(over = 1.seconds) {
                            name { text("Bad") }
                            every(Duration.ZERO)
                        }
                    }
                }
            }

            "rejects missing name" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                shouldThrow<IllegalStateException> {
                    context(ticker) {
                        audience.bossBar(over = 1.seconds) {
                            color(red)
                        }
                    }
                }.message shouldBe "'name' is not set."
            }

            listOf(
                "rejects a duplicate static name" to {
                    val ticker = ManualTicker()
                    context(ticker) {
                        TimedBossBarRecordingAudience().bossBar(over = 1.seconds) {
                            name { text("a") }
                            name { text("b") }
                        }
                    }
                },
                "rejects mixing static and dynamic name" to {
                    val ticker = ManualTicker()
                    context(ticker) {
                        TimedBossBarRecordingAudience().bossBar(over = 1.seconds) {
                            name { text("a") }
                            name { remaining -> text("$remaining") }
                        }
                    }
                },
                "rejects a duplicate progress" to {
                    val ticker = ManualTicker()
                    context(ticker) {
                        TimedBossBarRecordingAudience().bossBar(over = 1.seconds) {
                            name { text("a") }
                            progress(from = 1f, to = 0f)
                            progress(from = 0f, to = 1f)
                        }
                    }
                },
                "rejects a duplicate every" to {
                    val ticker = ManualTicker()
                    context(ticker) {
                        TimedBossBarRecordingAudience().bossBar(over = 1.seconds) {
                            name { text("a") }
                            every(1.ticks)
                            every(2.ticks)
                        }
                    }
                },
                "rejects a duplicate onTick" to {
                    val ticker = ManualTicker()
                    context(ticker) {
                        TimedBossBarRecordingAudience().bossBar(over = 1.seconds) {
                            name { text("a") }
                            onTick { }
                            onTick { }
                        }
                    }
                },
                "rejects a duplicate onFinish" to {
                    val ticker = ManualTicker()
                    context(ticker) {
                        TimedBossBarRecordingAudience().bossBar(over = 1.seconds) {
                            name { text("a") }
                            onFinish { }
                            onFinish { }
                        }
                    }
                },
                "rejects a duplicate onCancel" to {
                    val ticker = ManualTicker()
                    context(ticker) {
                        TimedBossBarRecordingAudience().bossBar(over = 1.seconds) {
                            name { text("a") }
                            onCancel { }
                            onCancel { }
                        }
                    }
                },
                "rejects a duplicate color" to {
                    val ticker = ManualTicker()
                    context(ticker) {
                        TimedBossBarRecordingAudience().bossBar(over = 1.seconds) {
                            name { text("a") }
                            color(red)
                            color(blue)
                        }
                    }
                },
            ).forEach { (name, action) ->
                name {
                    shouldThrow<IllegalStateException> { action() }
                }
            }

            "pause and resume after finish throw" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 1.seconds) {
                            name { text("Done") }
                            every(1.seconds)
                        }
                    }
                ticker.advance(1.seconds)

                shouldThrow<IllegalStateException> { timed.pause() }
                shouldThrow<IllegalStateException> { timed.resume() }
            }

            "pause and resume after cancel throw" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 10.seconds) {
                            name { text("X") }
                        }
                    }
                timed.cancel()

                shouldThrow<IllegalStateException> { timed.pause() }
                shouldThrow<IllegalStateException> { timed.resume() }
            }

            "double pause throws; resume without pause throws" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 10.seconds) {
                            name { text("X") }
                            every(1.seconds)
                        }
                    }

                shouldThrow<IllegalStateException> { timed.resume() }
                timed.pause()
                shouldThrow<IllegalStateException> { timed.pause() }
            }

            "hide removes a viewer from auto-hide tracking" {
                val ticker = ManualTicker()
                val creator = TimedBossBarRecordingAudience()
                val extra = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        creator.bossBar(over = 1.seconds) {
                            name { text("X") }
                            every(1.seconds)
                        }
                    }
                timed.show(extra)
                timed.hide(extra)
                extra.hidden.clear()

                ticker.advance(1.seconds)

                creator.hidden shouldContainExactly listOf(timed.bar)
                extra.hidden shouldHaveSize 0
            }

            "show adds an extra viewer immediately" {
                val ticker = ManualTicker()
                val creator = TimedBossBarRecordingAudience()
                val extra = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        creator.bossBar(over = 5.seconds) {
                            name { text("X") }
                        }
                    }
                timed.show(extra)

                extra.shown shouldContainExactly listOf(timed.bar)
            }

            "never re-pushes a fixed name" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()
                val nameChanges = mutableListOf<Component>()

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 3.seconds) {
                            name { text("Fixed") }
                            every(1.seconds)
                        }
                    }
                timed.bar.addListener(nameChangeListener(nameChanges))

                ticker.advance(3.seconds)

                nameChanges shouldHaveSize 0
            }

            "pushes a dynamic name only when the rendered component changes" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()
                val nameChanges = mutableListOf<Component>()

                val timed =
                    context(ticker) {
                        audience.bossBar(over = 4.seconds) {
                            // Initial 2; ticks render 2, 1, 1, 0 — only two actual changes.
                            name { remaining -> text("${(remaining.inWholeSeconds + 1) / 2}") }
                            every(1.seconds)
                        }
                    }
                timed.bar.addListener(nameChangeListener(nameChanges))

                ticker.advance(4.seconds)

                nameChanges shouldHaveSize 2
            }

            "show after cancel is a no-op and does not track the viewer" {
                val ticker = ManualTicker()
                val creator = TimedBossBarRecordingAudience()
                val late = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        creator.bossBar(over = 5.seconds) {
                            name { text("X") }
                            every(1.seconds)
                        }
                    }
                timed.cancel()
                timed.show(late)

                late.shown shouldHaveSize 0
                late.hidden shouldHaveSize 0
            }

            "show after natural completion is a no-op and does not track the viewer" {
                val ticker = ManualTicker()
                val creator = TimedBossBarRecordingAudience()
                val late = TimedBossBarRecordingAudience()

                val timed =
                    context(ticker) {
                        creator.bossBar(over = 1.seconds) {
                            name { text("X") }
                            every(1.seconds)
                        }
                    }
                ticker.advance(1.seconds)
                timed.isRunning shouldBe false

                timed.show(late)

                late.shown shouldHaveSize 0
                late.hidden shouldHaveSize 0
            }
        },
    )
