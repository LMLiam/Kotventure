package io.github.lmliam.kotventure.test.time

import kotlin.time.Duration

/**
 * Stores one immutable scheduled run for [ManualTicker].
 */
internal data class ManualTickerScheduleEntry(
    val dueAt: Duration,
    val sequence: Long,
    val task: ManualTickerTask,
)
