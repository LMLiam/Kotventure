package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.time.Ticker
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
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
    ticker: Ticker,
    config: TimedBossBarConfig,
    initialViewer: Audience,
) {
    /** The underlying Adventure boss bar; progress and name are updated each tick. */
    public val bar: BossBar = config.buildInitialBar()

    private val runtime = TimedBossBarRuntime(ticker, config, bar, this)

    /**
     * Time remaining until natural completion; frozen while [isPaused] and at the value it had
     * when [cancel] ended the bar early. [Duration.ZERO] after natural completion.
     */
    public val remaining: Duration by runtime::remaining

    /** `true` until the bar finishes naturally or is [cancel]led. */
    public val isRunning: Boolean by runtime::isRunning

    /** `true` after [pause] and before [resume], while still [isRunning]. */
    public val isPaused: Boolean by runtime::isPaused

    init {
        runtime.start(initialViewer)
    }

    /**
     * Freezes [remaining] and stops ticker wakeups until [resume].
     *
     * @throws IllegalStateException when the bar is finished, cancelled, or already paused.
     */
    public fun pause(): Unit = runtime.pause()

    /**
     * Continues from the frozen [remaining] after [pause].
     *
     * @throws IllegalStateException when the bar is finished, cancelled, or not paused.
     */
    public fun resume(): Unit = runtime.resume()

    /**
     * Stops the bar, hides it from all tracked viewers, and fires `onCancel` once when this call
     * ends a still-running bar. Idempotent after finish or a prior cancel.
     */
    public fun cancel(): Unit = runtime.cancel()

    /**
     * Shows [bar] to [audience] and tracks it for auto-hide on completion or [cancel].
     *
     * No-op when the bar is already finished or cancelled (not tracked, not shown).
     * Showing the same audience again while running re-invokes Adventure show and keeps a single tracking entry.
     */
    public fun show(audience: Audience): Unit = runtime.show(audience)

    /**
     * Hides [bar] from [audience] and stops tracking it for auto-hide.
     */
    public fun hide(audience: Audience): Unit = runtime.hide(audience)
}

private fun TimedBossBarConfig.buildInitialBar(): BossBar =
    BossBar.bossBar(name(over), progress.from, appearance.color, appearance.overlay, appearance.flags)
