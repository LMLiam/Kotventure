package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import org.bukkit.entity.Entity
import org.bukkit.plugin.Plugin
import java.util.function.Consumer
import kotlin.time.Duration

/**
 * Implements [Ticker] with the scheduler that follows [entity].
 *
 * The first run occurs after one interval. Scheduling fails with [IllegalStateException] if Paper
 * has retired the entity scheduler.
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
                { action() },
                null,
                ticks,
                ticks,
            )
        return ScheduledTickerTask(
            checkNotNull(scheduledTask) { "cannot schedule repeating work on removed entity $entity." },
        )
    }
}
