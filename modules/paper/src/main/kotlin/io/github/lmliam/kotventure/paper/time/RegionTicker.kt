package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import java.util.function.Consumer
import kotlin.time.Duration

/**
 * [Ticker] over the scheduler of the region containing [location]: repeating work becomes a
 * [runAtFixedRate][io.papermc.paper.threadedregions.scheduler.RegionScheduler.runAtFixedRate]
 * task owned by [plugin], with the first fire one full interval after scheduling.
 */
internal class RegionTicker(
    private val plugin: Plugin,
    private val location: Location,
) : Ticker {
    override fun repeating(
        interval: Duration,
        action: () -> Unit,
    ): TickerTask {
        val ticks = interval.wholeTicks()
        val scheduledTask =
            plugin.server.regionScheduler.runAtFixedRate(
                plugin,
                location,
                { action() },
                ticks,
                ticks,
            )
        return ScheduledTickerTask(scheduledTask)
    }
}
