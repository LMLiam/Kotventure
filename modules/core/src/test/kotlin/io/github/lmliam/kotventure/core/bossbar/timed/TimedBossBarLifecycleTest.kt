package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.StaleCallbackTicker
import io.github.lmliam.kotventure.test.bossbar.shouldHaveProgress
import io.github.lmliam.kotventure.test.time.ManualTicker
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import net.kyori.adventure.bossbar.BossBar
import kotlin.time.Duration
import kotlin.time.Duration.Companion.seconds

class TimedBossBarLifecycleTest :
    StringSpec(
        {
            "defaults to a 1->0 countdown and lands exactly on to" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, audience, 10.seconds) {
                        name { text("Countdown") }
                        every(1.seconds)
                    }

                timed.bar shouldHaveProgress BossBar.MAX_PROGRESS
                timed.remaining shouldBe 10.seconds
                timed.isRunning shouldBe true

                ticker.advance(5.seconds)

                timed.remaining shouldBe 5.seconds
                timed.bar.progress() shouldBe (0.5f plusOrMinus 0.001f)

                ticker.advance(5.seconds)

                timed.remaining shouldBe Duration.ZERO
                timed.bar shouldHaveProgress BossBar.MIN_PROGRESS
                timed.isRunning shouldBe false
                audience.hidden shouldContainExactly listOf(timed.bar)
            }

            "interpolates arbitrary from->to progress" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, audience, 4.seconds) {
                        name { text("Fill") }
                        progress(from = 0.25f, to = 0.75f)
                        every(1.seconds)
                    }

                timed.bar shouldHaveProgress 0.25f

                ticker.advance(2.seconds)

                timed.bar.progress() shouldBe (0.5f plusOrMinus 0.001f)

                ticker.advance(2.seconds)

                timed.bar shouldHaveProgress 0.75f
            }

            "cancel hides immediately and is idempotent" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()
                var cancels = 0

                val timed =
                    timedBossBar(ticker, audience, 10.seconds) {
                        name { text("Abort") }
                        every(1.seconds)
                        onCancel { cancels++ }
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

            "pause freezes remaining and resume continues" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, audience, 10.seconds) {
                        name { text("Hold") }
                        every(1.seconds)
                    }

                ticker.advance(3.seconds)
                timed.remaining shouldBe 7.seconds

                timed.pause()
                timed.isPaused shouldBe true

                ticker.advance(5.seconds)

                timed.remaining shouldBe 7.seconds
                timed.bar.progress() shouldBe (0.7f plusOrMinus 0.001f)

                timed.resume()
                timed.isPaused shouldBe false

                ticker.advance(2.seconds)

                timed.remaining shouldBe 5.seconds
            }

            "stale ticker after pause/resume does not advance remaining" {
                val ticker = StaleCallbackTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, audience, 10.seconds) {
                        name { text("Race") }
                        every(1.seconds)
                    }

                ticker.scheduledCount shouldBe 1
                ticker.run(0)
                timed.remaining shouldBe 9.seconds

                timed.pause()
                timed.resume()

                ticker.scheduledCount shouldBe 2
                ticker.run(0)
                timed.remaining shouldBe 9.seconds

                ticker.run(1)
                timed.remaining shouldBe 8.seconds
            }

            "pause and resume after finish throw" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, audience, 1.seconds) {
                        name { text("Done") }
                        every(1.seconds)
                    }

                ticker.advance(1.seconds)

                shouldThrow<IllegalStateException> { timed.pause() }
                shouldThrow<IllegalStateException> { timed.resume() }
            }

            "pause and resume after cancel throw" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, audience, 10.seconds) {
                        name { text("X") }
                    }

                timed.cancel()

                shouldThrow<IllegalStateException> { timed.pause() }
                shouldThrow<IllegalStateException> { timed.resume() }
            }

            "double pause throws and resume without pause throws" {
                val ticker = ManualTicker()
                val audience = TimedBossBarRecordingAudience()

                val timed =
                    timedBossBar(ticker, audience, 10.seconds) {
                        name { text("X") }
                        every(1.seconds)
                    }

                shouldThrow<IllegalStateException> { timed.resume() }

                timed.pause()

                shouldThrow<IllegalStateException> { timed.pause() }
            }
        },
    )
