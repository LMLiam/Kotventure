package io.github.lmliam.kotventure.paper.time

import net.kyori.adventure.util.Ticks
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Converts a positive duration that contains an exact number of 50-millisecond ticks.
 *
 * @throws IllegalArgumentException when the duration is not positive or not an exact number of
 *   ticks.
 */
internal fun Duration.wholeTicks(): Long {
    require(isPositive()) { "repeating interval must be positive, got $this." }
    val millis = inWholeMilliseconds
    require(this == millis.milliseconds && millis % Ticks.SINGLE_TICK_DURATION_MS == 0L) {
        "repeating interval must be a whole number of ticks " +
                "(${Ticks.SINGLE_TICK_DURATION_MS} ms each), got $this."
    }
    return millis / Ticks.SINGLE_TICK_DURATION_MS
}
