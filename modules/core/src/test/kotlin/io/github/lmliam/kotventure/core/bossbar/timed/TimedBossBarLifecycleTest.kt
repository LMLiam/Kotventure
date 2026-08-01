package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.time.StaleCallbackTicker
import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import io.github.lmliam.kotventure.test.bossbar.shouldHaveProgress
import io.github.lmliam.kotventure.test.time.ManualTicker
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.floats.plusOrMinus
import io.kotest.matchers.shouldBe
import net.kyori.adventure.audience.Audience
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

            "runs every shutdown step and preserves the first failure" {
                val cancellationFailure = IllegalStateException("cancel")
                val firstHideFailure = IllegalStateException("first hide")
                val secondHideFailure = IllegalStateException("second hide")
                val hookFailure = IllegalStateException("hook")
                val firstViewer = FailingHideAudience(firstHideFailure)
                val secondViewer = FailingHideAudience(secondHideFailure)

                val timed =
                    timedBossBar(
                        CancellationFailingTicker(cancellationFailure),
                        firstViewer,
                        1.seconds,
                    ) {
                        name { text("Failure") }
                        onCancel { throw hookFailure }
                    }
                timed.show(secondViewer)

                val thrown = shouldThrow<IllegalStateException> { timed.cancel() }

                thrown shouldBe cancellationFailure
                val hideFailures = setOf(firstHideFailure, secondHideFailure)
                val topLevelHideFailures = thrown.suppressed.filter { it in hideFailures }
                topLevelHideFailures.size shouldBe 1
                val topLevelHideFailure = topLevelHideFailures.single()
                topLevelHideFailure.suppressed.toSet() shouldBe hideFailures - setOf(topLevelHideFailure)
                thrown.suppressed.filter { it !== topLevelHideFailure }.toSet() shouldBe setOf(hookFailure)
                firstViewer.hideCalls shouldBe 1
                secondViewer.hideCalls shouldBe 1
                timed.isRunning shouldBe false
            }

            "natural completion runs every shutdown step and preserves the first failure" {
                val cancellationFailure = IllegalStateException("cancel")
                val firstViewerFailure = IllegalStateException("first hide")
                val secondViewerFailure = IllegalStateException("second hide")
                val finishFailure = IllegalStateException("finish")
                val hideOrder = mutableListOf<Throwable>()
                val firstViewer = FailingHideAudience(firstViewerFailure) { hideOrder += firstViewerFailure }
                val secondViewer = FailingHideAudience(secondViewerFailure) { hideOrder += secondViewerFailure }
                var finishCalls = 0
                var cancelCalls = 0
                val ticker = CancellationFailingTicker(cancellationFailure)

                val timed =
                    timedBossBar(
                        ticker,
                        firstViewer,
                        1.seconds,
                    ) {
                        name { text("Natural failure") }
                        every(1.seconds)
                        onFinish {
                            finishCalls++
                            throw finishFailure
                        }
                        onCancel { cancelCalls++ }
                    }
                timed.show(secondViewer)

                val thrown = shouldThrow<IllegalStateException> { ticker.run() }

                thrown shouldBe cancellationFailure
                hideOrder.size shouldBe 2
                hideOrder.toSet() shouldBe setOf(firstViewerFailure, secondViewerFailure)
                val firstHideFailure = hideOrder[0]
                val secondHideFailure = hideOrder[1]
                thrown.suppressed.toList() shouldBe listOf(firstHideFailure, finishFailure)
                firstHideFailure.suppressed.toList() shouldBe listOf(secondHideFailure)
                secondHideFailure.suppressed.toList() shouldBe emptyList()
                finishFailure.suppressed.toList() shouldBe emptyList()
                ticker.cancelCalls shouldBe 1
                cancelCalls shouldBe 0
                finishCalls shouldBe 1
                firstViewer.hideCalls shouldBe 1
                secondViewer.hideCalls shouldBe 1
                timed.isRunning shouldBe false
                timed.isPaused shouldBe false
                timed.remaining shouldBe Duration.ZERO

                ticker.run()
                timed.cancel()

                ticker.cancelCalls shouldBe 1
                cancelCalls shouldBe 0
                finishCalls shouldBe 1
                firstViewer.hideCalls shouldBe 1
                secondViewer.hideCalls shouldBe 1
            }
        },
    )

private class CancellationFailingTicker(
    private val failure: Throwable,
) : Ticker {
    override val isCurrent: Boolean = true
    private var scheduledAction: (() -> Unit)? = null
    var cancelCalls: Int = 0
        private set

    override fun every(
        interval: Duration,
        action: () -> Unit,
    ): TickerTask {
        scheduledAction = action
        return object : TickerTask {
            override fun cancel() {
                cancelCalls++
                throw failure
            }
        }
    }

    fun run() {
        checkNotNull(scheduledAction) { "The test ticker has no scheduled action." }.invoke()
    }

    override fun after(
        delay: Duration,
        action: () -> Unit,
    ): TickerTask = error("The test ticker does not support one-time tasks.")
}

private class FailingHideAudience(
    private val failure: Throwable,
    private val onHide: () -> Unit = {},
) : Audience {
    var hideCalls: Int = 0

    override fun showBossBar(bar: BossBar) = Unit

    override fun hideBossBar(bar: BossBar) {
        hideCalls++
        onHide()
        throw failure
    }
}
