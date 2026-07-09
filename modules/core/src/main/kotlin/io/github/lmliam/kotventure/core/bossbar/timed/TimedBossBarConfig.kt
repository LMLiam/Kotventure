package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.bossbar.BossBarAppearance
import kotlin.time.Duration

/**
 * Immutable configuration snapshot produced by [TimedBossBarBuilder] for [TimedBossBar].
 */
internal data class TimedBossBarConfig(
    val name: TimedBossBarNameSpec,
    val progressFrom: Float,
    val progressTo: Float,
    val appearance: BossBarAppearance,
    val every: Duration,
    val over: Duration,
    val onTick: (TimedBossBar.(Duration) -> Unit)?,
    val onFinish: (TimedBossBar.() -> Unit)?,
    val onCancel: (TimedBossBar.() -> Unit)?,
)
