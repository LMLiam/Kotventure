package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import org.bukkit.plugin.Plugin
import java.util.function.Consumer
import kotlin.time.Duration

/**
 * [Ticker] over the global region scheduler: repeating work becomes a
 * [runAtFixedRate][io.papermc.paper.threadedregions.scheduler.GlobalRegionScheduler.runAtFixedRate]
 * task owned by [plugin], with the first fire one full interval after scheduling.
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
                Consumer { action() },
                ticks,
                ticks,
            )
        return ScheduledTickerTask(scheduledTask)
    }
}
