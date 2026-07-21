package io.github.lmliam.kotventure.core.bossbar

import io.github.lmliam.kotventure.core.component.ComponentScope
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.ComponentLike

/**
 * Configures a static Adventure [BossBar]: required [name], optional [progress], and the shared
 * [appearance][BossBarAppearanceScope] slots.
 *
 * Only [name] is required. Unset optional slots default to a full pink continuous bar
 * ([BossBar.MAX_PROGRESS], [BossBar.Color.PINK], [BossBar.Overlay.PROGRESS], no flags).
 * Each slot and each flag may be set at most once.
 *
 * @sample io.github.lmliam.kotventure.core.bossbar.bossBarSample
 */
public interface BossBarScope : BossBarAppearanceScope {
    /**
     * Creates and sets the boss-bar name from a component DSL block.
     *
     * @throws IllegalStateException when the name is already set in this block.
     */
    public fun name(init: ComponentScope.() -> Unit)

    /**
     * Sets the boss bar name.
     *
     * @throws IllegalStateException when the name is already set in this block.
     */
    public fun <T : ComponentLike> name(component: T)

    /**
     * Sets fill amount in the inclusive range
     * [[BossBar.MIN_PROGRESS], [BossBar.MAX_PROGRESS]].
     *
     * Defaults to a full bar ([BossBar.MAX_PROGRESS]) when unset.
     *
     * @throws IllegalStateException when the progress is already set in this block.
     * @throws IllegalArgumentException when [progress] is outside `0f..1f`.
     */
    public fun progress(progress: Float)
}
