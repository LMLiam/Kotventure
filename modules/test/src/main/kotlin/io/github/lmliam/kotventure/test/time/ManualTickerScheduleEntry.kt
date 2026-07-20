package io.github.lmliam.kotventure.test.time

import kotlin.time.Duration

/**
 * One planned firing on a [ManualTicker] heap.
 *
 * Immutable so that the priority queue does not receive a changed sort key. Repeating work adds a new entry after each
 * successful run.
 */
internal data class ManualTickerScheduleEntry(
    val dueAt: Duration,
    val sequence: Long,
    val task: ManualTickerTask,
)
