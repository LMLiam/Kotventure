package io.github.lmliam.kotventure.core.bossbar

import io.github.lmliam.kotventure.core.color.hex
import io.github.lmliam.kotventure.core.text.text

internal fun bossBarSample() {
    bossBar {
        name {
            text("Ender Dragon") { color(hex("#9B30FF")) }
        }
        progress(0.25f)
        color(red)
        overlay(notched10)
        darkenScreen()
        playBossMusic()
    }
}
