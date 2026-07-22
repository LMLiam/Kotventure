package io.github.lmliam.kotventure.test.time

import kotlin.time.Duration

/**
 * Stores one immutable scheduled run for [ManualTicker].
 *
 * An [interval] of `null` marks a run that occurs one time.
 */
internal data class ManualTickerScheduleEntry(
    val dueAt: Duration,
    val sequence: Long,
    val task: ManualTickerTask,
    val interval: Duration?,
)
