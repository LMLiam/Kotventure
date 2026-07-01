package io.github.lmliam.kotventure.core.selector

/**
 * A floating-point range for selector arguments such as `distance`, `x_rotation`, and `y_rotation`.
 *
 * Construct open-ended or exact bounds via the helpers [atMost], [atLeast], and [exactly]; for a
 * closed range, pass a native Kotlin range to the consuming selector argument. Validation that
 * differs by argument is applied when the range is consumed. Integer-valued arguments such as
 * `level` and scoreboard objectives use the distinct [LevelRange] instead.
 */
public class SelectorRange internal constructor(
    internal val minimum: Double?,
    internal val maximum: Double?,
    internal val rendered: String,
) {
    public override fun equals(other: Any?): Boolean = other is SelectorRange && rendered == other.rendered

    public override fun hashCode(): Int = rendered.hashCode()

    public override fun toString(): String = rendered
}

/**
 * A range matching values up to and including [max] (renders as `..max`).
 *
 * @throws IllegalArgumentException if [max] is NaN or infinite
 */
public fun atMost(max: Double): SelectorRange {
    require(max.isFinite()) { "Range value must be finite, got: $max" }
    return SelectorRange(
        minimum = null,
        maximum = max,
        rendered = "..${formatSelectorNumber(max)}",
    )
}

/**
 * A range matching values at least [min] (renders as `min..`).
 *
 * @throws IllegalArgumentException if [min] is NaN or infinite
 */
public fun atLeast(min: Double): SelectorRange {
    require(min.isFinite()) { "Range value must be finite, got: $min" }
    return SelectorRange(
        minimum = min,
        maximum = null,
        rendered = "${formatSelectorNumber(min)}..",
    )
}

/**
 * A range matching exactly [value] (renders as `value`).
 *
 * @throws IllegalArgumentException if [value] is NaN or infinite
 */
public fun exactly(value: Double): SelectorRange {
    require(value.isFinite()) { "Range value must be finite, got: $value" }
    return SelectorRange(
        minimum = value,
        maximum = value,
        rendered = formatSelectorNumber(value),
    )
}

internal fun closedRange(
    min: Double,
    max: Double,
): SelectorRange {
    require(min.isFinite()) { "Range min must be finite, got: $min" }
    require(max.isFinite()) { "Range max must be finite, got: $max" }
    return SelectorRange(
        minimum = min,
        maximum = max,
        rendered = "${formatSelectorNumber(min)}..${formatSelectorNumber(max)}",
    )
}
