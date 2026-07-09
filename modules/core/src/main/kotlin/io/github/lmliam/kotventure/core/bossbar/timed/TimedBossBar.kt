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
        lock.withLock {
            check(running) { "Cannot pause a finished or cancelled TimedBossBar." }
            check(!paused) { "TimedBossBar is already paused." }
            paused = true
            stopTicking()
        }
    }

    /**
     * Continues from the frozen [remaining] after [pause].
     *
     * @throws IllegalStateException when the bar is finished, cancelled, or not paused.
     */
    public fun resume() {
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
        lock.withLock {
            if (!running) {
                return
            }
            stop()
        }
        // Runs outside the lock, like completion, so re-entrant handle calls can take it.
        config.onCancel?.invoke(this)
    }

    /**
     * Shows [bar] to [audience] and tracks it for auto-hide on completion or [cancel].
     *
     * No-op when the bar is already finished or cancelled (not tracked, not shown). Showing the
     * same audience again while running re-invokes Adventure show and keeps a single tracking
     * entry.
     */
    public fun show(audience: Audience) {
        lock.withLock {
            if (!running) {
                return
            }
            viewers.add(audience)
            audience.showBossBar(bar)
        }
    }

    /**
     * Hides [bar] from [audience] and stops tracking it for auto-hide.
     */
    public fun hide(audience: Audience) {
        lock.withLock {
            viewers.remove(audience)
            audience.hideBossBar(bar)
        }
    }

    private fun startTicking() {
        task = ticker.repeating(config.every) { tick() }
    }

    private fun stopTicking() {
        task?.cancel()
        task = null
    }

    private fun tick() {
        val remainingNow = lock.withLock { advanceOrNull() } ?: return

        // Hooks run outside the lock so re-entrant handle calls can take it.
        config.onTick?.invoke(this, remainingNow)

        if (remainingNow == Duration.ZERO) {
            completeNaturally()
        }
    }

    /**
     * Advances one interval and updates the bar, returning the new [remaining]; `null` when the
     * bar is no longer advancing. The re-rendered name is pushed only when it differs from the
     * current one, so fixed names and unchanged dynamic frames stay silent.
     */
    private fun advanceOrNull(): Duration? {
        if (!running || paused) {
            return null
        }
        remainingTime = (remainingTime - config.every).coerceAtLeast(Duration.ZERO)
        bar.progress(progressAt(remainingTime))
        val name = config.name(remainingTime)
        if (name != bar.name()) {
            bar.name(name)
        }
        return remainingTime
    }

    private fun completeNaturally() {
        lock.withLock {
            if (!running) {
                return
            }
            stop()
        }
        config.onFinish?.invoke(this)
    }

    /** Ends the bar: callers hold [lock] and have checked [running]. */
    private fun stop() {
        running = false
        paused = false
        stopTicking()
        val hidden = viewers.toList()
        viewers.clear()
        hidden.forEach { it.hideBossBar(bar) }
    }

    private fun progressAt(remaining: Duration): Float {
        if (remaining == Duration.ZERO) {
            return config.progressTo
        }
        val fraction = (1.0 - remaining / config.over).toFloat()
        return config.progressFrom + (config.progressTo - config.progressFrom) * fraction
    }
}
