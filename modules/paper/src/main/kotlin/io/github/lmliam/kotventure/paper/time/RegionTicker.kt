package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import org.bukkit.Location
import org.bukkit.plugin.Plugin
import java.util.function.Consumer
import kotlin.time.Duration

/**
 * Implements [Ticker] with the scheduler for the region that contains [location].
 *
 * Each scheduling call reads the location. The first run occurs after one interval.
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
