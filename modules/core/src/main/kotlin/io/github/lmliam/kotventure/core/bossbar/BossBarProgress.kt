package io.github.lmliam.kotventure.core.bossbar

import net.kyori.adventure.bossbar.BossBar

/**
 * Returns this value when it is in Adventure's inclusive boss-bar progress range.
 *
 * @throws IllegalArgumentException when this value is outside [[BossBar.MIN_PROGRESS], [BossBar.MAX_PROGRESS]].
 */
internal fun Float.requireBossBarProgress(label: String = "progress"): Float =
    also {
        require(this in BossBar.MIN_PROGRESS..BossBar.MAX_PROGRESS) {
            "'$label' must be in ${BossBar.MIN_PROGRESS}..${BossBar.MAX_PROGRESS}, got $this."
        }
    }
