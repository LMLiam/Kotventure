package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * Serialises the lifecycle and tick state of a [TimedBossBar].
 *
 * The lock protects running state, pause state, remaining time, viewer tracking, and the ticker task. Adventure show
 * and hide operations run outside the lock. Terminal hooks also run outside the lock.
 *
 * [owner] is still under construction when [start] schedules the first task. A ticker that invokes a task immediately
 * can expose that incomplete owner to an early hook. Runtime code must not assume that construction has returned when
 * the first hook starts.
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
     * Changes on every [detachTask] so a stale callback cannot update a resumed or terminated bar.
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
        // Schedule under the lock so that cancellation cannot create a replacement task.
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

        // A concurrent termination can occur after tracking but before the show operation completes.
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
     * Cancels the detached task, attempts to hide every snapshotted viewer, and then invokes [hook].
     *
     * Viewer failures do not stop later hide attempts. The function keeps the first failure and suppresses later
     * failures. The `finally` block guarantees that the terminal hook runs.
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
        task = ticker.every(config.every) { onTick(generation) }
    }

    private fun detachTask(): TickerTask? {
        tickGeneration++
        return task.also { task = null }
    }

    private fun onTick(generation: Int) {
        // Mark natural completion before the hook so concurrent cancellation cannot replace it.
        val outcome = lock.withLock { advanceOrNull(generation) } ?: return
        try {
            config.onTick?.invoke(owner, outcome.remaining)
        } finally {
            outcome.shutdown?.let { finaliseShutdown(it, config.onFinish) }
        }
    }

    /**
     * Applies one valid tick while [lock] is held.
     *
     * A tick at [Duration.ZERO] marks the bar as stopped and returns shutdown work for execution outside the lock. A
     * callback from a detached task returns `null` without changing state.
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
     * Marks the bar as stopped, detaches its task, and removes a snapshot of its viewers while [lock] is held.
     *
     * [finaliseShutdown] cancels the task and hides the snapshot after the caller releases the lock.
     */
    private fun markStopped(): TimedBossBarShutdown {
        running = false
        paused = false
        return TimedBossBarShutdown(
            task = detachTask(),
            viewers = viewers.snapshotAndClear(),
        )
    }

    /** Tick state that carries shutdown work only for natural completion. */
    private data class TickOutcome(
        val remaining: Duration,
        val shutdown: TimedBossBarShutdown?,
    )
}
