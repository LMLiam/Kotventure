package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.bossbar.BossBarAppearanceScope
import io.github.lmliam.kotventure.core.bossbar.BossBarScope
import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.ComponentLike
import kotlin.time.Duration

/**
 * Configures a lifecycle-managed [TimedBossBar]: required [name], interpolated [progress], update
 * cadence, lifecycle hooks, and the shared [appearance][BossBarAppearanceScope] slots.
 *
 * Static [progress][BossBarScope.progress] is intentionally absent — a managed bar owns its fill
 * amount over time. Unset [progress] defaults to a full → empty countdown; unset [every] defaults
 * to one game tick.
 *
 * @sample io.github.lmliam.kotventure.core.audience.timedBossBarSample
 */
public interface TimedBossBarScope : BossBarAppearanceScope {
    /**
     * Builds a fixed boss bar name from a component DSL block.
     *
     * @throws IllegalStateException when the name is already set in this block.
     */
    public fun name(init: ComponentScope.() -> Unit)

    /**
     * Sets a fixed boss bar name.
     *
     * @throws IllegalStateException when the name is already set in this block.
     */
    public fun <T : ComponentLike> name(component: T)

    /**
     * Re-renders the bar name every tick from the time remaining until completion.
     *
     * Call site: `name { remaining -> text("… ${remaining.inWholeSeconds}s") }`. The block is a
     * component scope exactly like the fixed `name { }` form — the extra `remaining` parameter
     * is the only difference. The SAM [TimedBossBarName] keeps this overload distinct from the
     * fixed forms during overload resolution. The rendered component is pushed to the bar only
     * when it differs from the current name, so unchanged frames cause no viewer updates.
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
     * @param from starting fill amount (inclusive `0f..1f`).
     * @param to ending fill amount (inclusive `0f..1f`).
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
     * @param interval positive delay between updates.
     * @throws IllegalStateException when the cadence is already set in this block.
     * @throws IllegalArgumentException when [interval] is not positive.
     */
    public fun every(interval: Duration)

    /**
     * Invoked after each progress update and name re-render while the bar is running.
     *
     * The [TimedBossBar] handle is the receiver; the lambda's `remaining` argument is the time
     * left after this tick's update. Runs on the ticker's thread.
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
