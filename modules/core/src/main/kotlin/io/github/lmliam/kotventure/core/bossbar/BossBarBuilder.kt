package io.github.lmliam.kotventure.core.bossbar

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.bossbar.BossBar
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike

internal class BossBarBuilder : BossBarScope {
    private var name: Component? by once()

    // Cannot be named `progress`: BossBarScope already has `val progress: Overlay`.
    private var progressValue: Float? by once { "'progress' is already set." }

    private var color: BossBar.Color? by once()
    private var overlay: BossBar.Overlay? by once()
    private var darkenScreen: Boolean? by once()
    private var playBossMusic: Boolean? by once()
    private var createWorldFog: Boolean? by once()

    override fun name(init: ComponentScope.() -> Unit): Unit = name(component(init))

    override fun <T : ComponentLike> name(component: T) {
        name = component.asComponent()
    }

    override fun progress(progress: Float) {
        progressValue = progress
    }

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

    internal fun build(): BossBar {
        val name = checkNotNull(name) { "'name' is not set." }
        val flags =
            buildSet {
                if (darkenScreen == true) add(BossBar.Flag.DARKEN_SCREEN)
                if (playBossMusic == true) add(BossBar.Flag.PLAY_BOSS_MUSIC)
                if (createWorldFog == true) add(BossBar.Flag.CREATE_WORLD_FOG)
            }
        return BossBar.bossBar(
            name,
            progressValue ?: BossBar.MAX_PROGRESS,
            color ?: BossBar.Color.PINK,
            overlay ?: BossBar.Overlay.PROGRESS,
            flags,
        )
    }
}
