package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock
import kotlin.time.Duration

/**
 * Handle for a lifecycle-managed boss bar that interpolates progress over a duration and
 * auto-hides when finished or cancelled.
 *
 * Built via [bossBar][io.github.lmliam.kotventure.core.audience.bossBar] with a contextual
 * [Ticker]. The underlying [bar] remains a live-mutable Adventure [BossBar]. Viewers added via
 * [show] are tracked so completion and [cancel] hide the bar from every tracked audience.
 */
public class TimedBossBar internal constructor(
    private val ticker: Ticker,
    private val config: TimedBossBarConfig,
    initialViewer: Audience,
) {
    /** The underlying Adventure boss bar; progress and name are updated each tick. */
    public val bar: BossBar =
        BossBar.bossBar(
            config.name(config.over),
            config.progressFrom,
            config.appearance.color,
            config.appearance.overlay,
            config.appearance.flags,
        )

    private val lock = ReentrantLock()
    private val viewers = mutableSetOf<Audience>()
    private var task: TickerTask? = null
    private var remainingTime = config.over
    private var running = true
    private var paused = false

    /**
     * Time remaining until natural completion; frozen while [isPaused] and at the value it had
     * when [cancel] ended the bar early. [Duration.ZERO] after natural completion.
     */
    public val remaining: Duration
        get() = lock.withLock { remainingTime }

    /** `true` until the bar finishes naturally or is [cancel]led. */
    public val isRunning: Boolean
        get() = lock.withLock { running }

    /** `true` after [pause] and before [resume], while still [isRunning]. */
    public val isPaused: Boolean
        get() = lock.withLock { paused }

    init {
        show(initialViewer)
        startTicking()
    }

    /**
     * Freezes [remaining] and stops ticker wakeups until [resume].
     *
     * @throws IllegalStateException when the bar is finished, cancelled, or already paused.
     */
    public fun pause() {
        val toCancel =
            lock.withLock {
                check(running) { "Cannot pause a finished or cancelled TimedBossBar." }
                check(!paused) { "TimedBossBar is already paused." }
                paused = true
                detachTask()
            }
        toCancel?.cancel()
    }

    /**
     * Continues from the frozen [remaining] after [pause].
     *
     * @throws IllegalStateException when the bar is finished, cancelled, or not paused.
     */
    public fun resume() {
        // Schedule under the lock so cancel cannot race a new task into existence after stop.
        lock.withLock {
            check(running) { "Cannot resume a finished or cancelled TimedBossBar." }
            check(paused) { "TimedBossBar is not paused." }
            paused = false
            startTicking()
        }
    }

    /**
     * Stops the bar, hides it from all tracked viewers, and fires `onCancel` once when this call
     * ends a still-running bar. Idempotent after finish or a prior cancel.
     */
    public fun cancel() {
        val shutdown =
            lock.withLock {
                if (!running) return
                markStopped()
            }
        finalizeShutdown(shutdown, config.onCancel)
    }

    /**
     * Shows [bar] to [audience] and tracks it for auto-hide on completion or [cancel].
     *
     * No-op when the bar is already finished or cancelled (not tracked, not shown). Showing the
     * same audience again while running re-invokes Adventure show and keeps a single tracking
     * entry.
     */
    public fun show(audience: Audience) {
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

    /**
     * Hides [bar] from [audience] and stops tracking it for auto-hide.
     */
    public fun hide(audience: Audience) {
        lock.withLock { viewers.remove(audience) }
        audience.hideBossBar(bar)
    }

    private fun startTicking() {
        task = ticker.repeating(config.every) { tick() }
    }

    private fun detachTask(): TickerTask? {
        val current = task
        task = null
        return current
    }

    private fun tick() {
        val remainingNow = lock.withLock { advanceOrNull() } ?: return
        config.onTick?.invoke(this, remainingNow)
        if (remainingNow == Duration.ZERO) completeNaturally()
    }

    /**
     * Advances one interval and updates the bar, returning the new [remaining]; `null` when the
     * bar is no longer advancing. The re-rendered name is pushed only when it differs from the
     * current one, so fixed names and unchanged dynamic frames stay silent.
     */
    private fun advanceOrNull(): Duration? {
        if (!running || paused) return null
        remainingTime = advanceTime()
        bar.progress(interpolateProgress(remainingTime))
        updateNameIfChanged(remainingTime)
        return remainingTime
    }

    private fun advanceTime(): Duration = (remainingTime - config.every).coerceAtLeast(Duration.ZERO)

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
        finalizeShutdown(shutdown, config.onFinish)
    }

    /**
     * Ends the bar under [lock]: clears running state, detaches the ticker task, and snapshots
     * viewers. Adventure hide and task cancel happen outside the lock via [finalizeShutdown].
     */
    private fun markStopped(): Shutdown {
        running = false
        paused = false
        val detached = detachTask()
        val snapshot = viewers.toList()
        viewers.clear()
        return Shutdown(task = detached, viewers = snapshot)
    }

    /**
     * Cancels the detached ticker task, hides every snapshotted viewer (isolating per-viewer
     * failures), then always runs the terminal [hook] once.
     */
    private fun finalizeShutdown(
        shutdown: Shutdown,
        hook: (TimedBossBar.() -> Unit)?,
    ) {
        shutdown.task?.cancel()
        try {
            hideAll(shutdown.viewers)
        } finally {
            hook?.invoke(this)
        }
    }

    private fun hideAll(audiences: List<Audience>) {
        var firstError: Throwable? = null
        for (audience in audiences) {
            try {
                audience.hideBossBar(bar)
            } catch (error: Throwable) {
                if (firstError == null) {
                    firstError = error
                } else {
                    firstError.addSuppressed(error)
                }
            }
        }
        firstError?.let { throw it }
    }

    private fun interpolateProgress(remaining: Duration): Float {
        if (remaining == Duration.ZERO) return config.progressTo
        val elapsed = 1.0 - (remaining / config.over)
        val delta = config.progressTo - config.progressFrom
        return config.progressFrom + (delta * elapsed).toFloat()
    }

    private data class Shutdown(
        val task: TickerTask?,
        val viewers: List<Audience>,
    )
}
