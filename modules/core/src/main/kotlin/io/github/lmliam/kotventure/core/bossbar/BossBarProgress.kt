package io.github.lmliam.kotventure.core.bossbar

import net.kyori.adventure.bossbar.BossBar

/**
 * Requires this float to lie in Adventure's inclusive boss-bar progress range.
 *
 * @throws IllegalArgumentException when outside [[BossBar.MIN_PROGRESS], [BossBar.MAX_PROGRESS]]
 */
internal fun Float.requireBossBarProgress(label: String = "progress"): Float =
    also {
        require(this in BossBar.MIN_PROGRESS..BossBar.MAX_PROGRESS) {
            "'$label' must be in ${BossBar.MIN_PROGRESS}..${BossBar.MAX_PROGRESS}, got $this."
        }
    }
