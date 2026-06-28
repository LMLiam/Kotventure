package io.github.lmliam.kotventure.core.selector

/**
 * A floating-point range for the `distance` selector argument.
 *
 * Construct open-ended or exact bounds via the helpers [atMost], [atLeast], and [exactly]; for a
 * closed range, pass a native Kotlin range to `distance(a..b)` directly. Integer-valued `level`
 * uses the distinct [LevelRange] instead.
 */
@JvmInline
public value class SelectorRange internal constructor(
    internal val rendered: String,
) {
    override fun toString(): String = rendered
}

/**
 * A range matching values up to and including [max] (renders as `..max`).
 *
 * @throws IllegalArgumentException if [max] is NaN or infinite
 */
public fun atMost(max: Double): SelectorRange {
    require(max.isFinite()) { "Range value must be finite, got: $max" }
    return SelectorRange("..${formatNumber(max)}")
}

/**
 * A range matching values at least [min] (renders as `min..`).
 *
 * @throws IllegalArgumentException if [min] is NaN or infinite
 */
public fun atLeast(min: Double): SelectorRange {
    require(min.isFinite()) { "Range value must be finite, got: $min" }
    return SelectorRange("${formatNumber(min)}..")
}

/**
 * A range matching exactly [value] (renders as `value`).
 *
 * @throws IllegalArgumentException if [value] is NaN or infinite
 */
public fun exactly(value: Double): SelectorRange {
    require(value.isFinite()) { "Range value must be finite, got: $value" }
    return SelectorRange(formatNumber(value))
}

internal fun closedRange(
    min: Double,
    max: Double,
): SelectorRange {
    require(min.isFinite()) { "Range min must be finite, got: $min" }
    require(max.isFinite()) { "Range max must be finite, got: $max" }
    require(min <= max) { "Range min ($min) must not exceed max ($max)" }
    return SelectorRange("${formatNumber(min)}..${formatNumber(max)}")
}

private fun formatNumber(value: Double): String =
    if (value == value.toLong().toDouble()) value.toLong().toString() else value.toString()
