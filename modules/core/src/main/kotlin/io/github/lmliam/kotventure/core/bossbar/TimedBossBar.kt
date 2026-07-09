package io.github.lmliam.kotventure.core.bossbar

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import java.util.Collections
import java.util.IdentityHashMap
import kotlin.time.Duration

/**
 * Handle for a lifecycle-managed boss bar that interpolates progress over a duration and
 * auto-hides when finished or cancelled.
 *
 * Built via [io.github.lmliam.kotventure.core.audience.bossBar] with a contextual
 * [Ticker][io.github.lmliam.kotventure.core.time.Ticker]. The underlying [bar] remains a
 * live-mutable Adventure [BossBar]. Viewers added via [show] are tracked so completion and
 * [cancel] hide the bar from every tracked audience.
 */
public class TimedBossBar internal constructor(
    private val ticker: Ticker,
    private val config: TimedBossBarConfig,
    creator: Audience,
) {
    /** The underlying Adventure boss bar; progress and name are updated each tick. */
    public val bar: BossBar =
        BossBar.bossBar(
            config.name.resolve(config.over),
            config.progressFrom,
            config.color,
            config.overlay,
            config.flags,
        )

    private val viewers: MutableSet<Audience> =
        Collections.newSetFromMap(IdentityHashMap())

    private val lock = Any()

    @Volatile
    private var elapsed: Duration = Duration.ZERO

    /**
     * Time remaining until natural completion; frozen while [isPaused] and at the value it had
     * when [cancel] ended the bar early. [Duration.ZERO] after natural completion.
     */
    @Volatile
    public var remaining: Duration = config.over
        private set

    /** `true` until the bar finishes naturally or is [cancel]led. */
    @Volatile
    public var isRunning: Boolean = true
        private set

    /** `true` after [pause] and before [resume], while still [isRunning]. */
    @Volatile
    public var isPaused: Boolean = false
        private set

    @Volatile
    private var task: TickerTask? = null

    init {
        show(creator)
        startTicking()
    }

    /**
     * Freezes [remaining] and stops ticker wakeups until [resume].
     *
     * @throws IllegalStateException when the bar is finished, cancelled, or already paused.
     */
    public fun pause() {
        synchronized(lock) {
            check(isRunning) { "Cannot pause a finished or cancelled TimedBossBar." }
            check(!isPaused) { "TimedBossBar is already paused." }
            isPaused = true
            stopTicking()
        }
    }

    /**
     * Continues from the frozen [remaining] after [pause].
     *
     * @throws IllegalStateException when the bar is finished, cancelled, or not paused.
     */
    public fun resume() {
        synchronized(lock) {
            check(isRunning) { "Cannot resume a finished or cancelled TimedBossBar." }
            check(isPaused) { "TimedBossBar is not paused." }
            isPaused = false
            startTicking()
        }
    }

    /**
     * Stops the bar, hides it from all tracked viewers, and fires `onCancel` once when this call
     * ends a still-running bar. Idempotent after finish or a prior cancel.
     */
    public fun cancel() {
        synchronized(lock) {
            if (!isRunning) {
                return
            }
            isRunning = false
            isPaused = false
            stopTicking()
            hideAllViewers()
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
        synchronized(lock) {
            if (!isRunning) {
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
        synchronized(lock) {
            viewers.remove(audience)
            audience.hideBossBar(bar)
        }
    }

    private fun startTicking() {
        task =
            ticker.repeating(config.every) {
                onInterval()
            }
    }

    private fun stopTicking() {
        task?.cancel()
        task = null
    }

    private fun onInterval() {
        val finished: Boolean
        val remainingAfter: Duration
        synchronized(lock) {
            if (!isRunning || isPaused) {
                return
            }
            elapsed += config.every
            if (elapsed >= config.over) {
                elapsed = config.over
                remaining = Duration.ZERO
                bar.progress(config.progressTo)
                applyName(Duration.ZERO)
                finished = true
                remainingAfter = Duration.ZERO
            } else {
                remaining = config.over - elapsed
                bar.progress(interpolatedProgress(elapsed))
                applyName(remaining)
                finished = false
                remainingAfter = remaining
            }
        }

        // Hooks run outside the lock so re-entrant handle calls can take it.
        config.onTick?.invoke(this, remainingAfter)

        if (finished) {
            completeNaturally()
        }
    }

    private fun completeNaturally() {
        synchronized(lock) {
            if (!isRunning) {
                return
            }
            isRunning = false
            isPaused = false
            stopTicking()
            hideAllViewers()
        }
        config.onFinish?.invoke(this)
    }

    private fun hideAllViewers() {
        val snapshot = viewers.toList()
        viewers.clear()
        for (audience in snapshot) {
            audience.hideBossBar(bar)
        }
    }

    private fun applyName(remaining: Duration) {
        val spec = config.name
        if (spec is BossBarNameSpec.Dynamic) {
            bar.name(spec.resolve(remaining))
        }
    }

    private fun interpolatedProgress(elapsed: Duration): Float {
        val overNanos = config.over.inWholeNanoseconds.toDouble()
        val fraction = (elapsed.inWholeNanoseconds.toDouble() / overNanos).toFloat()
        return config.progressFrom + (config.progressTo - config.progressFrom) * fraction
    }
}
