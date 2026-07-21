package io.github.lmliam.kotventure.core.bossbar

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.bossbar.BossBar

/**
 * Configures the strip colour, overlay, and screen-effect flags of a boss bar.
 *
 * This scope does not contain progress or name slots. The static and time-managed scopes use different forms of these
 * slots. Each concrete scope declares its applicable form so that the compiler rejects an incorrect form.
 *
 * Scope-bound [BossBar.Color] and [BossBar.Overlay] values, such as [red] and [notched10], are available only in this
 * DSL scope. [KotventureDslMarker] prevents a nested component scope from using them by mistake.
 */
@KotventureDslMarker
public interface BossBarAppearanceScope {
    /**
     * Sets the colour of the bar strip. This setting does not change the colour of the name text.
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
     * The continuous [BossBar.Overlay.PROGRESS] overlay, without notches.
     *
     * Coexists with the float `progress` slots on the concrete scopes. `progress(0.25f)` resolves to the function.
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
