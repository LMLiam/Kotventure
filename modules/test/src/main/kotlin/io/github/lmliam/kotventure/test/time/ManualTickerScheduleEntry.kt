package io.github.lmliam.kotventure.test.time

import kotlin.time.Duration

/**
 * One planned firing on a [ManualTicker] heap.
 *
 * Immutable so the priority queue never sees a mutated sort key; repeating work re-offers a new
 * entry after each successful fire.
 */
internal data class ManualTickerScheduleEntry(
    val dueAt: Duration,
    val sequence: Long,
    val task: ManualTickerTask,
)
