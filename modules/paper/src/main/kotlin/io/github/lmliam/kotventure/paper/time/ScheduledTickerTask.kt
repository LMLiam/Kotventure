package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.TickerTask
import io.papermc.paper.threadedregions.scheduler.ScheduledTask

/**
 * [TickerTask] over a scheduled [ScheduledTask]; [cancel] delegates to the Paper task, which is
 * itself safe to cancel repeatedly and from any thread.
 */
internal class ScheduledTickerTask(
    private val scheduledTask: ScheduledTask,
) : TickerTask {
    override fun cancel() {
        scheduledTask.cancel()
    }
}
