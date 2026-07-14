package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.TickerTask
import org.bukkit.scheduler.BukkitTask

/**
 * [TickerTask] over a scheduled [BukkitTask]; [cancel] delegates to the Bukkit task, which is
 * itself idempotent and thread-safe.
 */
internal class PaperTickerTask(
    private val bukkitTask: BukkitTask,
) : TickerTask {
    override fun cancel(): Unit = bukkitTask.cancel()
}
