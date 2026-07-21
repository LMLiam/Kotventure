package io.github.lmliam.kotventure.core.bossbar

import net.kyori.adventure.bossbar.BossBar

/**
 * Resolved colour, overlay, and flags for one boss bar.
 *
 * Construction replaces unset scope values with Adventure's pink, continuous, no-flag defaults.
 */
internal data class BossBarAppearance(
    val color: BossBar.Color,
    val overlay: BossBar.Overlay,
    val flags: Set<BossBar.Flag>,
)
