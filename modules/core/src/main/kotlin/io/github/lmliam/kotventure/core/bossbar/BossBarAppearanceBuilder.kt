package io.github.lmliam.kotventure.core.bossbar

import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.bossbar.BossBar

/**
 * Collects the [BossBarAppearanceScope] slots. Concrete boss-bar builders compose this object through interface
 * delegation and get the resolved snapshot from [build].
 */
internal class BossBarAppearanceBuilder : BossBarAppearanceScope {
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

    fun build(): BossBarAppearance =
        BossBarAppearance(
            color = color ?: BossBar.Color.PINK,
            overlay = overlay ?: BossBar.Overlay.PROGRESS,
            flags =
                buildSet {
                    if (darkenScreen == true) add(BossBar.Flag.DARKEN_SCREEN)
                    if (playBossMusic == true) add(BossBar.Flag.PLAY_BOSS_MUSIC)
                    if (createWorldFog == true) add(BossBar.Flag.CREATE_WORLD_FOG)
                },
        )
}
