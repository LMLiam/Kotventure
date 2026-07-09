package io.github.lmliam.kotventure.core.bossbar

import net.kyori.adventure.bossbar.BossBar

/**
 * Immutable snapshot of the shared bar-strip look, with unset slots resolved to Adventure's
 * classic full-pink-bar defaults.
 */
internal data class BossBarAppearance(
    val color: BossBar.Color,
    val overlay: BossBar.Overlay,
    val flags: Set<BossBar.Flag>,
)
