package io.github.lmliam.kotventure.test.time

import io.github.lmliam.kotventure.core.time.TickerTask

/**
 * Stores the action and the cancellation state of one [ManualTicker] task.
 *
 * [ManualTickerScheduleEntry] holds the due time and the recurrence of each run.
 */
internal class ManualTickerTask(
    private val action: () -> Unit,
) : TickerTask {
    var isActive: Boolean = true
        private set

    /**
     * Runs the action one time.
     *
     * @throws Throwable when the action throws it.
     */
    fun run() {
        action()
    }

    override fun cancel() {
        isActive = false
    }
}
