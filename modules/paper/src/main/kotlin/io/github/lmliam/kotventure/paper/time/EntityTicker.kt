package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.util.function.Consumer
import kotlin.time.Duration

/**
 * [Ticker] over [entity]'s scheduler: repeating work becomes a
 * [runAtFixedRate][io.papermc.paper.threadedregions.scheduler.EntityScheduler.runAtFixedRate]
 * task owned by [plugin] that follows the entity's region, with the first fire one full interval
 * after scheduling. Scheduling on an already-removed entity throws [IllegalStateException].
 */
internal class EntityTicker(
    private val plugin: Plugin,
    private val entity: Entity,
) : Ticker {
    override fun repeating(
        interval: Duration,
        action: () -> Unit,
    ): TickerTask {
        val ticks = interval.wholeTicks()
        val scheduledTask =
            entity.scheduler.runAtFixedRate(
                plugin,
                Consumer { action() },
                null,
                ticks,
                ticks,
            )
        return ScheduledTickerTask(
            checkNotNull(scheduledTask) { "cannot schedule repeating work on removed entity $entity." },
        )
    }
}
