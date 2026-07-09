package io.github.lmliam.kotventure.core.bossbar

import net.kyori.adventure.bossbar.BossBar

/**
 * Configures a static Adventure [BossBar]: required name, optional [progress]/colour/overlay, and
 * optional flag toggles.
 *
 * Only [name][BossBarBaseScope.name] is required. Unset optional slots default to a full pink
 * continuous bar ([BossBar.MAX_PROGRESS], [BossBar.Color.PINK], [BossBar.Overlay.PROGRESS], no
 * flags). Each slot and each flag may be set at most once.
 *
 * @sample io.github.lmliam.kotventure.core.bossbar.bossBarSample
 */
public interface BossBarScope : BossBarBaseScope {
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
}
