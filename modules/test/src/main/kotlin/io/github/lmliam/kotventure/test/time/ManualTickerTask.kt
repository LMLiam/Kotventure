package io.github.lmliam.kotventure.test.time

import io.github.lmliam.kotventure.core.time.TickerTask
import kotlin.time.Duration

/**
 * A [ManualTicker] repeating unit: fires [action] and, while still active, reports the next due
 * time as `dueAt + interval`.
 */
internal class ManualTickerTask(
    private val interval: Duration,
    private val action: () -> Unit,
) : TickerTask {
    var isActive: Boolean = true
        private set

    /**
     * Invokes [action] once at virtual time [dueAt].
     *
     * @return next due time (`dueAt + interval`) when the task should keep repeating, or `null`
     *   when it was already cancelled or cancelled itself during [action].
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
