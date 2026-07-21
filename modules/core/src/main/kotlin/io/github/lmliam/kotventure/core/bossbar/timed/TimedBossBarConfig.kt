package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.bossbar.BossBarAppearance
import net.kyori.adventure.text.Component
import kotlin.time.Duration

/**
 * Immutable runtime configuration for one [TimedBossBar].
 *
 * [name] produces a bar name for a remaining duration. A fixed name returns the same component for every call. The
 * runtime compares each result with the current name and skips an unchanged update.
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
