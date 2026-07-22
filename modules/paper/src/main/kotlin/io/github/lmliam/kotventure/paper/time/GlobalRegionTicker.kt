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
    override val isCurrent: Boolean
        get() = plugin.server.isGlobalTickThread

    override fun every(
        interval: Duration,
        action: () -> Unit,
    ): TickerTask {
        val ticks = interval.wholeTicks("every interval")
        val scheduledTask =
            plugin.server.globalRegionScheduler.runAtFixedRate(
                plugin,
                { action() },
                ticks,
                ticks,
            )
        return ScheduledTickerTask(scheduledTask)
    }

    override fun after(
        delay: Duration,
        action: () -> Unit,
    ): TickerTask {
        val ticks = delay.afterTicks()
        val scheduler = plugin.server.globalRegionScheduler
        val scheduledTask =
            if (ticks == null) {
                scheduler.run(plugin) { action() }
            } else {
                scheduler.runDelayed(plugin, { action() }, ticks)
            }
        return ScheduledTickerTask(scheduledTask)
    }
}
