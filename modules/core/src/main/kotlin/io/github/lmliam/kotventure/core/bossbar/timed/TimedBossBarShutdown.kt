package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.time.TickerTask
import net.kyori.adventure.audience.Audience

/**
 * Detached task and viewer snapshot produced when a [TimedBossBar] terminates.
 *
 * The runtime uses this value after it releases its state lock.
 */
internal data class TimedBossBarShutdown(
    val task: TickerTask?,
    val viewers: List<Audience>,
)
