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
 * A task from [once] with a delay of zero is due at the current time. It runs on the next [advance].
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

    /**
     * Shows if the caller is inside [advance].
     *
     * A scheduled action reads `true` here. Test code outside [advance] reads `false`.
     */
    override val ownsCurrentThread: Boolean
        get() = isAdvancing

    /** Marks the period in which [advance] runs scheduled actions. */
    private var isAdvancing: Boolean = false

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
     * in registration order. A task can cancel itself or another task during its action. A task can
     * also schedule more work, which runs in the same advance if its due time is in the period.
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
        isAdvancing = true
        try {
            while (true) {
                val next = pollNextDueAtOrBefore(end) ?: break
                currentTime = next.dueAt
                next.task.run()
                rescheduleIfRepeating(next)
            }
            currentTime = end
        } finally {
            isAdvancing = false
        }
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
        val task = ManualTickerTask(action)
        requeue(task = task, dueAt = currentTime + interval, interval = interval)
        return task
    }

    /**
     * Schedules [action] to run one time after [delay] of virtual time.
     *
     * The run is due at the current time plus [delay]. A delay of zero is due at the current time.
     * Only [advance] can run the action. The returned task can cancel the run before it starts.
     *
     * @throws IllegalArgumentException when [delay] is negative.
     */
    override fun once(
        delay: Duration,
        action: () -> Unit,
    ): TickerTask {
        require(!delay.isNegative()) { "once delay must not be negative, got $delay." }
        val task = ManualTickerTask(action)
        requeue(task = task, dueAt = currentTime + delay, interval = null)
        return task
    }

    /** Queues the next run of [entry] when the entry repeats and its task is still active. */
    private fun rescheduleIfRepeating(entry: ManualTickerScheduleEntry) {
        if (!entry.task.isActive) {
            return
        }
        val interval = entry.interval ?: return
        requeue(task = entry.task, dueAt = entry.dueAt + interval, interval = interval)
    }

    private fun requeue(
        task: ManualTickerTask,
        dueAt: Duration,
        interval: Duration?,
    ) {
        schedule +=
            ManualTickerScheduleEntry(
                dueAt = dueAt,
                sequence = nextSequence++,
                task = task,
                interval = interval,
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
