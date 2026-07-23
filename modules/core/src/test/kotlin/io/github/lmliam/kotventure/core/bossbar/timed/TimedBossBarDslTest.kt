package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.ticks
import io.github.lmliam.kotventure.test.bossbar.shouldHaveColor
import io.github.lmliam.kotventure.test.bossbar.shouldHaveOverlay
import io.github.lmliam.kotventure.test.time.ManualTicker
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.bossbar.BossBar
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TimedBossBarDslTest :
    StringSpec(
        {
            "configures colour and overlay like a static bar" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, audience, 1.seconds) {
                        name { text("Styled") }
                        color(red)
                        overlay(notched10)
                    }

                timed.bar shouldHaveColor BossBar.Color.RED
                timed.bar shouldHaveOverlay BossBar.Overlay.NOTCHED_10
            }

            "rejects non-positive over" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                shouldThrow<IllegalArgumentException> {
                    timedBossBar(ticker, audience, Duration.ZERO) {
                        name { text("Bad") }
                    }
                }
                shouldThrow<IllegalArgumentException> {
                    timedBossBar(ticker, audience, (-1).seconds) {
                        name { text("Bad") }
                    }
                }
            }

            "rejects out-of-range progress endpoints" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                shouldThrow<IllegalArgumentException> {
                    timedBossBar(ticker, audience, 1.seconds) {
                        name { text("Bad") }
                        progress(from = 1.5f, to = 0f)
                    }
                }
                shouldThrow<IllegalArgumentException> {
                    timedBossBar(ticker, audience, 1.seconds) {
                        name { text("Bad") }
                        progress(from = 0f, to = -0.1f)
                    }
                }
            }

            "rejects non-positive every" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                shouldThrow<IllegalArgumentException> {
                    timedBossBar(ticker, audience, 1.seconds) {
                        name { text("Bad") }
                        every(Duration.ZERO)
                    }
                }
            }

            "rejects every larger than over" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                shouldThrow<IllegalArgumentException> {
                    timedBossBar(ticker, audience, 1.seconds) {
                        name { text("Bad") }
                        every(2.seconds)
                    }
                }
            }

            "rejects missing name" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                shouldThrow<IllegalStateException> {
                    timedBossBar(ticker, audience, 1.seconds) {
                        color(red)
                    }
                }
            }

            listOf(
                "duplicate static name" to {
                    timedBossBar(ManualTicker(), TimedBossBarRecordingAudience(), 1.seconds) {
                        name { text("a") }
                        name { text("b") }
                    }
                },
                "mixed static and dynamic name" to {
                    timedBossBar(ManualTicker(), TimedBossBarRecordingAudience(), 1.seconds) {
                        name { text("a") }
                        name { remaining -> text("$remaining") }
                    }
                },
                "duplicate progress" to {
                    timedBossBar(ManualTicker(), TimedBossBarRecordingAudience(), 1.seconds) {
                        name { text("a") }
                        progress(from = 1f, to = 0f)
                        progress(from = 0f, to = 1f)
                    }
                },
                "duplicate every" to {
                    timedBossBar(ManualTicker(), TimedBossBarRecordingAudience(), 1.seconds) {
                        name { text("a") }
                        every(1.ticks)
                        every(2.ticks)
                    }
                },
                "duplicate onTick" to {
                    timedBossBar(ManualTicker(), TimedBossBarRecordingAudience(), 1.seconds) {
                        name { text("a") }
                        onTick { }
                        onTick { }
                    }
                },
                "duplicate onFinish" to {
                    timedBossBar(ManualTicker(), TimedBossBarRecordingAudience(), 1.seconds) {
                        name { text("a") }
                        onFinish { }
                        onFinish { }
                    }
                },
                "duplicate onCancel" to {
                    timedBossBar(ManualTicker(), TimedBossBarRecordingAudience(), 1.seconds) {
                        name { text("a") }
                        onCancel { }
                        onCancel { }
                    }
                },
                "duplicate color" to {
                    timedBossBar(ManualTicker(), TimedBossBarRecordingAudience(), 1.seconds) {
                        name { text("a") }
                        color(red)
                        color(blue)
                    }
                },
            ).forEach { (name, action) ->
                "rejects a $name" {
                    shouldThrow<IllegalStateException> { action() }
                }
            }
        },
    )
