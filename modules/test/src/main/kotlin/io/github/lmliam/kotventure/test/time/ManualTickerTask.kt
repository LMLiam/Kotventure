package io.github.lmliam.kotventure.test.time

import io.github.lmliam.kotventure.core.time.TickerTask
import kotlin.time.Duration

/**
 * Stores the action and state of one repeating [ManualTicker] task.
 */
internal class ManualTickerTask(
    private val interval: Duration,
    private val action: () -> Unit,
) : TickerTask {
    var isActive: Boolean = true
        private set

    /**
     * Runs [action] one time for the scheduled time [dueAt].
     *
     * @return the next due time, or `null` if the task is cancelled.
     * @throws Throwable when [action] throws it.
     */
    fun fire(dueAt: Duration): Duration? {
        if (!isActive) {
            return null
        }
        action()
        return if (isActive) dueAt + interval else null
    }

    override fun cancel() {
        isActive = false
    }
}
