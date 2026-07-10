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
 * Owns running/paused state, remaining time, viewer tracking, and the ticker task. Adventure
 * show/hide and terminal hooks run outside the lock where noted by the public facade.
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
                paused = true
                detachTask()
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

    fun cancel(): TimedBossBarShutdown? =
        lock.withLock {
            if (!running) return null
            markStopped()
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

        // If cancel/finish raced between track and show, undo the visible bar.
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
    fun finaliseShutdown(
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
        task = ticker.repeating(config.every) { onTick() }
    }

    private fun detachTask(): TickerTask? {
        val current = task
        task = null
        return current
    }

    private fun onTick() {
        val remainingNow = lock.withLock { advanceOrNull() } ?: return
        config.onTick?.invoke(owner, remainingNow)
        if (remainingNow == Duration.ZERO) completeNaturally()
    }

    private fun advanceOrNull(): Duration? {
        if (!running || paused) return null
        remainingTime = (remainingTime - config.every).coerceAtLeast(Duration.ZERO)
        bar.progress(config.progress.at(remaining = remainingTime, over = config.over))
        updateNameIfChanged(remainingTime)
        return remainingTime
    }

    private fun updateNameIfChanged(remaining: Duration) {
        val name = config.name(remaining)
        if (name != bar.name()) bar.name(name)
    }

    private fun completeNaturally() {
        val shutdown =
            lock.withLock {
                if (!running) return
                markStopped()
            }
        finaliseShutdown(shutdown, config.onFinish)
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
}
