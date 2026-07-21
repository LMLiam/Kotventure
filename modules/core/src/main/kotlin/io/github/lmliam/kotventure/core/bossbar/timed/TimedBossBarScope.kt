package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.bossbar.BossBarAppearanceScope
import io.github.lmliam.kotventure.core.bossbar.BossBarScope
import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.ComponentLike
import kotlin.time.Duration

/**
 * Configures the name, progress, cadence, hooks, and appearance of a [TimedBossBar].
 *
 * Static [progress][BossBarScope.progress] is intentionally absent. A managed bar owns its fill
 * amount over time. Unset [progress] gives a full-to-empty countdown. Unset [every] gives one game tick.
 *
 * @sample io.github.lmliam.kotventure.core.audience.timedBossBarSample
 */
public interface TimedBossBarScope : BossBarAppearanceScope {
    /**
     * Creates and sets a fixed boss-bar name from a component DSL block.
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
     * Sets a name renderer that receives the remaining lifetime after each update.
     *
     * Call site: `name { remaining -> text("… ${remaining.inWholeSeconds}s") }`. The function updates the mutable bar
     * only when the rendered component differs from its current name.
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
     * Defaults to one game tick when unset. Must not exceed the bar's `over` lifetime. Each
     * tick subtracts this interval from remaining time.
     *
     * @param interval the positive delay between updates. It must be `<= over` when the bar is built.
     * @throws IllegalStateException when the cadence is already set in this block.
     * @throws IllegalArgumentException when [interval] is not positive, or when it exceeds `over`
     *   at build time.
     */
    public fun every(interval: Duration)

    /**
     * Invoked after each progress update and name re-render while the bar is running.
     *
     * The [TimedBossBar] handle is the receiver. The `remaining` argument is the time after this update. The lambda runs
     * on the ticker's thread. On the final tick, it runs before viewer removal and [onFinish]. A thrown exception does
     * not prevent final viewer removal or [onFinish].
     *
     * @throws IllegalStateException when this hook is already set in this block.
     */
    public fun onTick(handler: TimedBossBar.(remaining: Duration) -> Unit)

    /**
     * Invoked once when the bar reaches the end of its lifetime naturally.
     *
     * It does not run when [TimedBossBar.cancel] ends the bar early. The [TimedBossBar] handle is the receiver. The hook
     * runs on the ticker's thread after the runtime attempts to hide all tracked viewers.
     *
     * @throws IllegalStateException when this hook is already set in this block.
     */
    public fun onFinish(handler: TimedBossBar.() -> Unit)

    /**
     * Invoked once when [TimedBossBar.cancel] ends the bar early.
     *
     * It does not run on natural completion. The [TimedBossBar] handle is the receiver. The hook runs on the cancelling
     * thread after the runtime attempts to hide all tracked viewers.
     *
     * @throws IllegalStateException when this hook is already set in this block.
     */
    public fun onCancel(handler: TimedBossBar.() -> Unit)
}
