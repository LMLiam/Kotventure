package io.github.lmliam.kotventure.core.selector

/**
 * A numeric range for selector arguments like `distance` and `level`.
 *
 * Construct via the helpers [atMost], [atLeast], [between], and [exactly].
 */
public class SelectorRange internal constructor(
    internal val rendered: String,
) {
    override fun toString(): String = rendered
}

/**
 * A range matching values up to and including [max] (renders as `..max`).
 */
public fun atMost(max: Double): SelectorRange = SelectorRange("..${formatNumber(max)}")

/**
 * A range matching values at least [min] (renders as `min..`).
 */
public fun atLeast(min: Double): SelectorRange = SelectorRange("${formatNumber(min)}..")

/**
 * A range matching values between [min] and [max] inclusive (renders as `min..max`).
 */
public fun between(
    min: Double,
    max: Double,
): SelectorRange = SelectorRange("${formatNumber(min)}..${formatNumber(max)}")

/**
 * A range matching exactly [value] (renders as `value`).
 */
public fun exactly(value: Double): SelectorRange = SelectorRange(formatNumber(value))

private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
