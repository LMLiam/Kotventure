package io.github.lmliam.kotventure.core.bossbar.timed

import io.github.lmliam.kotventure.core.time.TickerTask
import net.kyori.adventure.audience.Audience

/**
 * Snapshot taken when a [TimedBossBar] leaves the running state: the detached ticker task and
 * the viewers that still need to be hidden outside the bar's lock.
 */
internal data class TimedBossBarShutdown(
    val task: TickerTask?,
    val viewers: List<Audience>,
)
