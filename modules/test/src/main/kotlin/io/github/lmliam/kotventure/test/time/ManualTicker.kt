package io.github.lmliam.kotventure.test.time

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import java.util.PriorityQueue
import kotlin.time.Duration

/**
 * Provides a deterministic [Ticker] for single-threaded unit tests.
 *
 * Virtual time changes only when [advance] changes it. Due tasks run in chronological order. Tasks
 * with the same due time run in registration order.
 *
 * A repeating task keeps its original phase. Its next due time is its previous due time plus the
 * interval. Thus, one large advance runs every missed interval without drift.
 *
 * This class is not thread-safe.
 */
public class ManualTicker : Ticker {
    /**
     * Gets the current virtual time.
     *
     * Only [advance] changes this value. The ticker does not use wall-clock time.
     */
    public var currentTime: Duration = Duration.ZERO
        private set

    /** Keeps registration order for tasks that have the same due time. */
    private var nextSequence: Long = 0L

    /** Contains pending runs in due-time and registration order. */
    private val schedule: PriorityQueue<ManualTickerScheduleEntry> =
        PriorityQueue(
            compareBy(
                ManualTickerScheduleEntry::dueAt,
                ManualTickerScheduleEntry::sequence,
            ),
        )

    /**
     * Advances virtual time by [duration] and runs all tasks due in that period.
 *
     * The method runs tasks in chronological order. If tasks have the same due time, it runs them
     * in registration order. A task can cancel itself or another task during its action.
     *
     * If an action throws an exception, this method propagates it. Virtual time remains at that
     * action's due time, and the failed repeating task is not scheduled again.
 *
     * @throws IllegalArgumentException when [duration] is negative.
     * @throws Throwable when a scheduled action throws it.
     */
    public fun advance(duration: Duration) {
        require(!duration.isNegative()) { "advance duration must not be negative, got $duration." }
        if (duration == Duration.ZERO) {
            return
        }

        val end = currentTime + duration
        while (true) {
            val next = pollNextDueAtOrBefore(end) ?: break
            currentTime = next.dueAt
            next.task
                .fire(dueAt = next.dueAt)
                ?.let { requeue(task = next.task, dueAt = it) }
        }
        currentTime = end
    }

    /**
     * Schedules [action] after each [interval] of virtual time.
     *
     * The first run is due at the current time plus [interval]. Only [advance] can run the action.
     * The returned task can cancel future runs.
     *
     * @throws IllegalArgumentException when [interval] is not positive.
     */
    override fun repeating(
        interval: Duration,
        action: () -> Unit,
    ): TickerTask {
        require(interval.isPositive()) { "repeating interval must be positive, got $interval." }
        val task = ManualTickerTask(interval = interval, action = action)
        requeue(task = task, dueAt = currentTime + interval)
        return task
    }

    private fun requeue(
        task: ManualTickerTask,
        dueAt: Duration,
    ) {
        schedule +=
            ManualTickerScheduleEntry(
                dueAt = dueAt,
                sequence = nextSequence++,
                task = task,
            )
    }

    /** Returns and removes the first active entry due at or before [limit]. */
    private fun pollNextDueAtOrBefore(limit: Duration): ManualTickerScheduleEntry? {
        while (true) {
            val head = schedule.peek() ?: return null
            if (head.dueAt > limit) {
                return null
            }
            schedule.poll()
            if (head.task.isActive) {
                return head
            }
        }
    }
}
