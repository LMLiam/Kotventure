package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.audience.hide
import io.github.lmliam.kotventure.core.audience.show
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.time.ManualTicker
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import kotlin.time.Duration.Companion.seconds

class TimedBossBarAudienceTest :
    StringSpec(
        {
            "auto-hides from all tracked audiences on completion" {
                val ticker = ManualTicker()
                val creator = TimedBossBarRecordingAudience()
                val extra = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, creator, 1.seconds) {
                        name { text("Raid") }
                        every(1.seconds)
                    }

                timed.show(extra)

                ticker.advance(1.seconds)

                creator.hidden shouldContainExactly listOf(timed.bar)
                extra.hidden shouldContainExactly listOf(timed.bar)
                timed.isRunning shouldBe false
            }

            "hide removes a viewer from auto-hide tracking" {
                val ticker = ManualTicker()
                val creator = TimedBossBarRecordingAudience()
                val extra = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, creator, 1.seconds) {
                        name { text("X") }
                        every(1.seconds)
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
                    timedBossBar(ticker, creator, 5.seconds) {
                        name { text("X") }
                    }

                timed.show(extra)

                extra.shown shouldContainExactly listOf(timed.bar)
            }

            "audience show and hide verbs mirror the handle" {
                val ticker = ManualTicker()
                val creator = TimedBossBarRecordingAudience()
                val spectator = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, creator, 5.seconds) {
                        name { text("X") }
                        every(1.seconds)
                    }

                spectator.show(timed)

                spectator.shown shouldContainExactly listOf(timed.bar)

                spectator.hide(timed)

                spectator.hidden shouldContainExactly listOf(timed.bar)

                ticker.advance(5.seconds)

                spectator.hidden shouldHaveSize 1
            }

            "show after cancel is a no-op and does not track the viewer" {
                val ticker = ManualTicker()
                val creator = TimedBossBarRecordingAudience()
                val late = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, creator, 5.seconds) {
                        name { text("X") }
                        every(1.seconds)
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
                    timedBossBar(ticker, creator, 1.seconds) {
                        name { text("X") }
                        every(1.seconds)
                    }

                ticker.advance(1.seconds)
                timed.isRunning shouldBe false

                timed.show(late)

                late.shown shouldHaveSize 0
                late.hidden shouldHaveSize 0
            }

            "cancel hides remaining viewers and fires onCancel when one hide fails" {
                val ticker = ManualTicker()
                val creator = TimedBossBarRecordingAudience()
                val healthy = TimedBossBarRecordingAudience()
                val broken = ThrowingHideAudience()
                var cancels = 0

                val timed =
                    timedBossBar(ticker, creator, 10.seconds) {
                        name { text("X") }
                        every(1.seconds)
                        onCancel { cancels++ }
                    }

                timed.show(broken)
                timed.show(healthy)

                shouldThrow<IllegalStateException> {
                    timed.cancel()
                }

                cancels shouldBe 1
                healthy.hidden shouldContainExactly listOf(timed.bar)
                creator.hidden shouldContainExactly listOf(timed.bar)
                timed.isRunning shouldBe false
            }
        },
    )
