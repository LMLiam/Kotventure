package io.github.lmliam.kotventure.core.bossbar

import net.kyori.adventure.bossbar.BossBar
import kotlin.time.Duration

/**
 * Configures a lifecycle-managed [TimedBossBar]: shared name/colour/overlay/flag slots plus
 * interpolated [progress], update cadence, and lifecycle hooks.
 *
 * Static [progress][BossBarScope.progress] is intentionally absent — a managed bar owns its fill
 * amount over time. Unset [progress] defaults to a full → empty countdown; unset [every] defaults
 * to one game tick.
 *
 * @sample io.github.lmliam.kotventure.core.audience.timedBossBarSample
 */
public interface TimedBossBarScope : BossBarBaseScope {
    /**
     * Re-renders the bar name every tick from [remaining] time until completion.
     *
     * Call site: `name { remaining -> text("… ${remaining.inWholeSeconds}s") }`. The SAM
     * [TimedBossBarName] keeps this overload distinct from the static
     * [name][BossBarBaseScope.name] forms (component DSL block / existing component).
     *
     * @throws IllegalStateException when the name is already set in this block.
     */
    public fun name(render: TimedBossBarName)

    /**
     * Sets the progress endpoints interpolated linearly over the bar's lifetime.
     *
     * Each endpoint is validated in
     * [[BossBar.MIN_PROGRESS], [BossBar.MAX_PROGRESS]] (fail fast, never clamped).
     * Unset defaults to [BossBar.MAX_PROGRESS] → [BossBar.MIN_PROGRESS] (countdown).
     * The final tick lands exactly on [to].
     *
     * @throws IllegalStateException when progress is already set in this block.
     * @throws IllegalArgumentException when [from] or [to] is outside `0f..1f`.
     */
    public fun progress(
        from: Float = BossBar.MAX_PROGRESS,
        to: Float = BossBar.MIN_PROGRESS,
    )

    /**
     * Sets how often the bar updates (progress, dynamic name, [onTick]).
     *
     * Defaults to one game tick when unset.
     *
     * @throws IllegalStateException when the cadence is already set in this block.
     * @throws IllegalArgumentException when [interval] is not positive.
     */
    public fun every(interval: Duration)

    /**
     * Invoked after each progress update and name re-render while the bar is running.
     *
     * The [TimedBossBar] handle is the receiver; [remaining] is the time left after this tick's
     * update. Runs on the ticker's thread.
     *
     * @throws IllegalStateException when this hook is already set in this block.
     */
    public fun onTick(handler: TimedBossBar.(remaining: Duration) -> Unit)

    /**
     * Invoked once when the bar reaches the end of its lifetime naturally.
     *
     * Does not run when [TimedBossBar.cancel] ends the bar early. The [TimedBossBar] handle is the
     * receiver. Runs on the ticker's thread.
     *
     * @throws IllegalStateException when this hook is already set in this block.
     */
    public fun onFinish(handler: TimedBossBar.() -> Unit)

    /**
     * Invoked once when [TimedBossBar.cancel] ends the bar early.
     *
     * Does not run on natural completion. The [TimedBossBar] handle is the receiver. Runs on the
     * ticker's thread.
     *
     * @throws IllegalStateException when this hook is already set in this block.
     */
    public fun onCancel(handler: TimedBossBar.() -> Unit)
}
