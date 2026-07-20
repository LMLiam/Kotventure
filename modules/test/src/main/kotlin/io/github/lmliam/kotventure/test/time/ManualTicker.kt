package io.github.lmliam.kotventure.test.time

import io.github.lmliam.kotventure.core.time.Ticker
import io.github.lmliam.kotventure.core.time.TickerTask
import java.util.PriorityQueue
import kotlin.time.Duration

/**
 * Deterministic [Ticker] for unit tests: virtual time moves only via [advance].
 *
 * Due repeating work runs in schedule order. The earliest due work runs first, and equal times keep registration order.
 * The ticker preserves phase. Each run enters the queue again at `previousDue + interval`, not
 * `currentTime + interval`, so jumping multiple intervals does not drift.
 *
 * Modeled after the virtual-clock loop in kotlinx-coroutines-test's
 * `TestCoroutineScheduler.advanceTimeBy` (event heap + advance-to-next-due), without a coroutines
 * dependency.
 *
 * Thread-safety: single-threaded test use only.
 */
public class ManualTicker : Ticker {
    /**
     * Current virtual time.
     *
     * Advances only through [advance]. It does not use wall-clock time.
     */
    public var currentTime: Duration = Duration.ZERO
        private set

    /** Breaks ties so two tasks due at the same instant fire in registration order. */
    private var nextSequence: Long = 0L

    /**
     * A min-heap of pending runs. Entries are immutable. A repeating task adds a new
     * [ManualTickerScheduleEntry] after each successful fire instead of mutating an in-heap key.
     */
    private val schedule: PriorityQueue<ManualTickerScheduleEntry> =
        PriorityQueue(
            compareBy(
                ManualTickerScheduleEntry::dueAt,
                ManualTickerScheduleEntry::sequence,
            ),
        )

    /**
     * Advances the virtual clock by [duration] and runs every task due at or before the new
     * clock value, in chronological order.
     *
     * A task that stays active after its action is re-queued at `dueAt + interval`.
     * Cancelled heads are dropped lazily when they reach the front of the heap.
     *
     * @throws IllegalArgumentException when [duration] is negative.
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
     * Schedules [action] for each [interval] of manual time. The first run is due at
     * current time + [interval] and fires only when [advance] crosses it.
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

    /**
     * Pops the earliest still-active schedule entry with `dueAt <= limit`, discarding cancelled
     * heads along the way. Returns `null` when nothing is due by [limit].
     */
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
