package io.github.lmliam.kotventure.core.bossbar

import net.kyori.adventure.bossbar.BossBar
import kotlin.time.Duration

/**
 * Immutable configuration snapshot produced by [TimedBossBarBuilder] for [TimedBossBar].
 */
internal data class TimedBossBarConfig(
    val name: BossBarNameSpec,
    val progressFrom: Float,
    val progressTo: Float,
    val color: BossBar.Color,
    val overlay: BossBar.Overlay,
    val flags: Set<BossBar.Flag>,
    val every: Duration,
    val over: Duration,
    val onTick: (TimedBossBar.(Duration) -> Unit)?,
    val onFinish: (TimedBossBar.() -> Unit)?,
    val onCancel: (TimedBossBar.() -> Unit)?,
)
