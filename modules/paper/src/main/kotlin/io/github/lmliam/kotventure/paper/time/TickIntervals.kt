package io.github.lmliam.kotventure.paper.time

import net.kyori.adventure.util.Ticks
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Converts a positive duration that contains an exact number of 50-millisecond ticks.
 *
 * @param name the argument to identify in a failure message.
 * @throws IllegalArgumentException when the duration is not positive or not an exact number of
 *   ticks.
 */
internal fun Duration.wholeTicks(name: String): Long {
    require(isPositive()) { "$name must be positive, got $this." }
    val millis = inWholeMilliseconds
    require(this == millis.milliseconds && millis % Ticks.SINGLE_TICK_DURATION_MS == 0L) {
        "$name must be a whole number of ticks " +
                "(${Ticks.SINGLE_TICK_DURATION_MS} ms each), got $this."
    }
    return millis / Ticks.SINGLE_TICK_DURATION_MS
}

/**
 * Converts a one-time delay into scheduler ticks.
 *
 * @return `null` when the delay is zero, which selects the next tick.
 * @throws IllegalArgumentException when the delay is negative or not an exact number of ticks.
 */
internal fun Duration.afterTicks(): Long? {
    require(!isNegative()) { "after delay must not be negative, got $this." }
    return if (this == Duration.ZERO) null else wholeTicks("after delay")
}
