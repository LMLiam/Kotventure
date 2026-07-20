package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.TickerTask
import io.papermc.paper.threadedregions.scheduler.ScheduledTask

/**
 * A [TickerTask] over a scheduled [ScheduledTask]. [cancel] delegates to the Paper task. You can safely cancel the Paper
 * task more than one time and from all threads.
 */
internal class ScheduledTickerTask(
    private val scheduledTask: ScheduledTask,
) : TickerTask {
    override fun cancel() {
        scheduledTask.cancel()
    }
}
