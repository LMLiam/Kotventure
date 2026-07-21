package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.time.Ticker
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.bossbar.BossBar
import kotlin.time.Duration

/**
 * Controls a boss bar that updates over a fixed lifetime and hides itself at termination.
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
    /** The mutable Adventure boss bar that receives each progress and name update. */
    public val bar: BossBar = config.buildInitialBar()

    private val runtime = TimedBossBarRuntime(ticker, config, bar, this)

    /**
     * Time remaining until natural completion. It does not change while [isPaused]. If [cancel] ends the bar early, it
     * keeps the value at cancellation. It is [Duration.ZERO] after natural completion.
     */
    public val remaining: Duration by runtime::remaining

    /** `true` until natural completion or the first [cancel] call. */
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
     * Stops updates, hides all tracked viewers, and invokes `onCancel` when this call terminates a running bar.
     *
     * The function does nothing after natural completion or an earlier cancellation. It invokes `onCancel` even when
     * one or more viewers fail to hide, then propagates a hide or hook failure.
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
     * Hides [bar] from [audience] and stops tracking that audience.
     *
     * The function forwards the hide operation even when the audience is not tracked or the bar has terminated.
     */
    public fun hide(audience: Audience): Unit = runtime.hide(audience)
}

private fun TimedBossBarConfig.buildInitialBar(): BossBar =
    BossBar.bossBar(name(over), progress.from, appearance.color, appearance.overlay, appearance.flags)
