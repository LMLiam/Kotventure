package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.TickerTask
import org.bukkit.scheduler.BukkitTask

/**
 * [TickerTask] over a scheduled [BukkitTask]; the first [cancel] cancels the Bukkit task,
 * later calls are no-ops.
 */
internal class PaperTickerTask(
        private val bukkitTask: BukkitTask,
) : TickerTask {
    private var cancelled = false

    override fun cancel() {
        if (cancelled) {
            return
        }
        cancelled = true
        bukkitTask.cancel()
    }
}
