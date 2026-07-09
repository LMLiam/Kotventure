package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.bossbar.BossBarAppearance
import net.kyori.adventure.text.Component
import kotlin.time.Duration

/**
 * Immutable configuration snapshot produced by [TimedBossBarBuilder] for [TimedBossBar].
 *
 * [name] renders the bar name for a remaining duration. Fixed names ignore the argument and
 * return the same component every call, so the bar's change-detection skips redundant pushes.
 */
internal data class TimedBossBarConfig(
    val name: (Duration) -> Component,
    val progress: TimedBossBarProgress,
    val appearance: BossBarAppearance,
    val every: Duration,
    val over: Duration,
    val onTick: (TimedBossBar.(Duration) -> Unit)?,
    val onFinish: (TimedBossBar.() -> Unit)?,
    val onCancel: (TimedBossBar.() -> Unit)?,
)
