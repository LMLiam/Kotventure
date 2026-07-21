package io.github.lmliam.kotventure.paper.time

import io.github.lmliam.kotventure.core.time.TickerTask
import io.papermc.paper.threadedregions.scheduler.ScheduledTask

/**
 * Implements [TickerTask] with a Paper [ScheduledTask].
 *
 * [cancel] stops future runs. It does not interrupt a run that is in progress.
 */
internal class ScheduledTickerTask(
    private val scheduledTask: ScheduledTask,
) : TickerTask {
    override fun cancel() {
        scheduledTask.cancel()
    }
}
