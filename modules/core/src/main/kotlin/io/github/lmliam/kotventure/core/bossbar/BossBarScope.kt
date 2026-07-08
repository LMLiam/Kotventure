package io.github.lmliam.kotventure.core.bossbar

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.ComponentLike

/**
 * Configures an Adventure [BossBar]: required [name], optional [progress]/[color]/[overlay], and
 * optional flag toggles.
 *
 * Only [name] is required. Unset optional slots default to a full pink continuous bar
 * ([BossBar.MAX_PROGRESS], [BossBar.Color.PINK], [BossBar.Overlay.PROGRESS], no flags).
 * Each slot and each flag may be set at most once.
 *
 * Scope-bound [BossBar.Color] and [BossBar.Overlay] vals (`red`, `notched10`, …) shadow top-level
 * text colours only inside this block; nested [name] component scopes cannot see them thanks to
 * [KotventureDslMarker].
 *
 * @sample io.github.lmliam.kotventure.core.bossbar.bossBarSample
 */
@KotventureDslMarker
public interface BossBarScope {
    /**
     * Builds the boss bar name from a component DSL block.
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
     * Out-of-range values fail fast via Adventure's own bounds check (not clamped).
     *
     * @throws IllegalStateException when progress is already set in this block.
     * @throws IllegalArgumentException when [progress] is outside `0f..1f`.
     */
    public fun progress(progress: Float)

    /**
     * Sets the bar colour (the strip colour — not the name text colour).
     *
     * Prefer the scope-bound vals ([pink], [red], …) so no enum import is needed.
     *
     * @throws IllegalStateException when the colour is already set in this block.
     */
    public fun color(color: BossBar.Color)

    /**
     * Sets how the bar is segmented.
     *
     * Prefer the scope-bound vals ([progress], [notched6], …) so no enum import is needed.
     *
     * @throws IllegalStateException when the overlay is already set in this block.
     */
    public fun overlay(overlay: BossBar.Overlay)

    /**
     * Enables [BossBar.Flag.DARKEN_SCREEN].
     *
     * @throws IllegalStateException when this flag is already set in this block.
     */
    public fun darkenScreen()

    /**
     * Enables [BossBar.Flag.PLAY_BOSS_MUSIC].
     *
     * @throws IllegalStateException when this flag is already set in this block.
     */
    public fun playBossMusic()

    /**
     * Enables [BossBar.Flag.CREATE_WORLD_FOG].
     *
     * @throws IllegalStateException when this flag is already set in this block.
     */
    public fun createWorldFog()

    /** [BossBar.Color.PINK]. */
    public val pink: BossBar.Color
        get() = BossBar.Color.PINK

    /** [BossBar.Color.BLUE]. */
    public val blue: BossBar.Color
        get() = BossBar.Color.BLUE

    /** [BossBar.Color.RED]. */
    public val red: BossBar.Color
        get() = BossBar.Color.RED

    /** [BossBar.Color.GREEN]. */
    public val green: BossBar.Color
        get() = BossBar.Color.GREEN

    /** [BossBar.Color.YELLOW]. */
    public val yellow: BossBar.Color
        get() = BossBar.Color.YELLOW

    /** [BossBar.Color.PURPLE]. */
    public val purple: BossBar.Color
        get() = BossBar.Color.PURPLE

    /** [BossBar.Color.WHITE]. */
    public val white: BossBar.Color
        get() = BossBar.Color.WHITE

    /**
     * Continuous [BossBar.Overlay.PROGRESS] overlay (no notches).
     *
     * Coexists with [progress] the float setter: `progress(0.25f)` resolves to the function;
     * `overlay(progress)` resolves to this property.
     */
    public val progress: BossBar.Overlay
        get() = BossBar.Overlay.PROGRESS

    /** [BossBar.Overlay.NOTCHED_6]. */
    public val notched6: BossBar.Overlay
        get() = BossBar.Overlay.NOTCHED_6

    /** [BossBar.Overlay.NOTCHED_10]. */
    public val notched10: BossBar.Overlay
        get() = BossBar.Overlay.NOTCHED_10

    /** [BossBar.Overlay.NOTCHED_12]. */
    public val notched12: BossBar.Overlay
        get() = BossBar.Overlay.NOTCHED_12

    /** [BossBar.Overlay.NOTCHED_20]. */
    public val notched20: BossBar.Overlay
        get() = BossBar.Overlay.NOTCHED_20
}
