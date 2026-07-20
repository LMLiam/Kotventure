package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * Lock-guarded lifecycle and tick progression for a [TimedBossBar].
 *
 * Owns running/paused state, remaining time, viewer tracking, and the ticker task.
Adventure
 * show/hide and terminal hooks run outside the lock.
 *
 * **This-escape:** [owner] is the constructing [TimedBossBar]. [start] may schedule
ticks before
 * that constructor returns, so [TimedBossBarConfig.onTick] can observe [owner]
mid-construction
 * on a real scheduler thread. Callers must not assume the facade is fully initialised
inside
 * early hooks.
 */
internal class TimedBossBarRuntime(
    private val ticker: Ticker,
    private val config: TimedBossBarConfig,
    private val bar: BossBar,
    private val owner: TimedBossBar,
) {
    private val lock = ReentrantLock()
    private val viewers = TimedBossBarViewers()
    private var task: TickerTask? = null
    private var remainingTime = config.over
    private var running = true
    private var paused = false

    /**
     * Invalidated on every [detachTask] so a cancelled/stale ticker callback cannot advance after
     * pause/resume schedules a replacement (cancel alone does not abort an in-flight action).
     */
    private var tickGeneration: Int = 0

    val remaining: Duration
        get() = lock.withLock { remainingTime }

    val isRunning: Boolean
        get() = lock.withLock { running }

    val isPaused: Boolean
        get() = lock.withLock { paused }

    fun start(initialViewer: Audience) {
        show(initialViewer)
        startTicking()
    }

    fun pause() {
        val toCancel =
            lock.withLock {
                check(running) { "Cannot pause a finished or cancelled TimedBossBar." }
                check(!paused) { "TimedBossBar is already paused." }
                detachTask().also { paused = true }
            }
        toCancel?.cancel()
    }

    fun resume() {
        // Schedule under the lock so cancel cannot race a new task into existence after stop.
        lock.withLock {
            check(running) { "Cannot resume a finished or cancelled TimedBossBar." }
            check(paused) { "TimedBossBar is not paused." }
            paused = false
            startTicking()
        }
    }

    fun cancel() {
        val shutdown = lock.withLock { if (running) markStopped() else null } ?: return
        finaliseShutdown(shutdown, config.onCancel)
    }

    fun show(audience: Audience) {
        val accepted =
            lock.withLock {
                if (!running) {
                    false
                } else {
                    viewers.add(audience)
                    true
                }
            }
        if (!accepted) return

        audience.showBossBar(bar)

        // If cancel/finish raced between track and show, undo the visible bar
        val stillTracked = lock.withLock { running && audience in viewers }
        if (!stillTracked) {
            audience.hideBossBar(bar)
        }
    }

    fun hide(audience: Audience) {
        lock.withLock { viewers.remove(audience) }
        audience.hideBossBar(bar)
    }

    /**
     * Cancels the detached ticker task, hides every snapshotted viewer (isolating per-viewer
     * failures), then always runs the terminal [hook] once.
     */
    private fun finaliseShutdown(
        shutdown: TimedBossBarShutdown,
        hook: (TimedBossBar.() -> Unit)?,
    ) {
        shutdown.task?.cancel()
        try {
            viewers.hideAll(bar, shutdown.viewers)
        } finally {
            hook?.invoke(owner)
        }
    }

    private fun startTicking() {
        val generation = tickGeneration
        task = ticker.repeating(config.every) { onTick(generation) }
    }

    private fun detachTask(): TickerTask? {
        tickGeneration++
        return task.also { task = null }
    }

    private fun onTick(generation: Int) {
        // At ZERO, mark natural stop under the same lock as the tick so cancel() from onTick
        // cannot steal completion (onFinish must win) and a thrown onTick still finalises hide.
        val outcome = lock.withLock { advanceOrNull(generation) } ?: return
        try {
            config.onTick?.invoke(owner, outcome.remaining)
        } finally {
            outcome.shutdown?.let { finaliseShutdown(it, config.onFinish) }
        }
    }

    /**
     * Advances remaining time under [lock]. When the tick lands on [Duration.ZERO], atomically
     * marks the bar stopped and returns the [TimedBossBarShutdown] for outside-lock finalisation.
     * Stale generation (detached tasks) are ignored.
     */
    private fun advanceOrNull(generation: Int): TickOutcome? {
        if (!running || paused || generation != tickGeneration) return null
        remainingTime = (remainingTime - config.every).coerceAtLeast(Duration.ZERO)
        bar.progress(config.progress.at(remaining = remainingTime, over = config.over))
        updateNameIfChanged(remainingTime)
        val shutdown = if (remainingTime == Duration.ZERO) markStopped() else null
        return TickOutcome(remaining = remainingTime, shutdown = shutdown)
    }

    private fun updateNameIfChanged(remaining: Duration) {
        val name = config.name(remaining)
        if (name != bar.name()) bar.name(name)
    }

    /**
     * Ends the bar under [lock]: clears running state, detaches the ticker task, and snapshots
     * viewers. Adventure hide and task cancel happen outside the lock via [finaliseShutdown].
     */
    private fun markStopped(): TimedBossBarShutdown {
        running = false
        paused = false
        return TimedBossBarShutdown(
            task = detachTask(),
            viewers = viewers.snapshotAndClear(),
        )
    }

    /** The result of one locked tick. [shutdown] is non-null only after natural completion. */
    private data class TickOutcome(
        val remaining: Duration,
        val shutdown: TimedBossBarShutdown?,
    )
}
