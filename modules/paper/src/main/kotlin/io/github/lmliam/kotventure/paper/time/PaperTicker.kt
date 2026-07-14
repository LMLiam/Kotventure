package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import net.kyori.adventure.util.Ticks
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitScheduler
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * [Ticker] over the Bukkit scheduler: repeating work becomes a synchronous
 * [runTaskTimer][BukkitScheduler.runTaskTimer] task owned by [plugin], with
 * the first fire one full interval after scheduling
 */
internal class PaperTicker(
    private val plugin: Plugin,
) : Ticker {
    private val scheduler: BukkitScheduler = plugin.server.scheduler

    override fun repeating(
        interval: Duration,
        action: () -> Unit,
    ): TickerTask {
        val ticks = interval.wholeTicks()
        val bukkitTask = scheduler.runTaskTimer(plugin, action, ticks, ticks)
        return PaperTickerTask(bukkitTask)
    }

    /**
     * The Bukkit scheduler cannot express sub-tick periods, so anything that is not a positive
     * whole number of ticks is rejected
     */
    private fun Duration.wholeTicks(): Long {
        require(isPositive()) { "repeating interval must be positive, got $this." }
        val millis = inWholeMilliseconds
        require(this == millis.milliseconds && millis % Ticks.SINGLE_TICK_DURATION_MS == 0L) {
            "repeating interval must be a whole number of ticks " +
                    "(${Ticks.SINGLE_TICK_DURATION_MS} ms each), got $this."
        }
        return millis / Ticks.SINGLE_TICK_DURATION_MS
    }
}
