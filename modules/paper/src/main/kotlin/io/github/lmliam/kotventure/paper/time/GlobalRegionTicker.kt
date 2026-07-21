package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import org.bukkit.plugin.Plugin
import java.util.function.Consumer
import kotlin.time.Duration

/**
 * Implements [Ticker] with the global region scheduler for [plugin].
 *
 * The first run occurs after one interval.
 */
internal class GlobalRegionTicker(
    private val plugin: Plugin,
) : Ticker {
    override fun repeating(
        interval: Duration,
        action: () -> Unit,
    ): TickerTask {
        val ticks = interval.wholeTicks()
        val scheduledTask =
            plugin.server.globalRegionScheduler.runAtFixedRate(
                plugin,
                { action() },
                ticks,
                ticks,
            )
        return ScheduledTickerTask(scheduledTask)
    }
}
