package io.github.lmliam.kotventure.core.bossbar

import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.bossbar.BossBar

/**
 * Shared builder state for [BossBarBaseScope] colour, overlay, and flag slots.
 */
internal abstract class BossBarBaseBuilder : BossBarBaseScope {
    private var color: BossBar.Color? by once()
    private var overlay: BossBar.Overlay? by once()
    private var darkenScreen: Boolean? by once()
    private var playBossMusic: Boolean? by once()
    private var createWorldFog: Boolean? by once()

    override fun color(color: BossBar.Color) {
        this.color = color
    }

    override fun overlay(overlay: BossBar.Overlay) {
        this.overlay = overlay
    }

    override fun darkenScreen() {
        darkenScreen = true
    }

    override fun playBossMusic() {
        playBossMusic = true
    }

    override fun createWorldFog() {
        createWorldFog = true
    }

    protected fun resolvedColor(): BossBar.Color = color ?: BossBar.Color.PINK

    protected fun resolvedOverlay(): BossBar.Overlay = overlay ?: BossBar.Overlay.PROGRESS

    protected fun resolvedFlags(): Set<BossBar.Flag> =
        buildSet {
            if (darkenScreen == true) add(BossBar.Flag.DARKEN_SCREEN)
            if (playBossMusic == true) add(BossBar.Flag.PLAY_BOSS_MUSIC)
            if (createWorldFog == true) add(BossBar.Flag.CREATE_WORLD_FOG)
        }
}
