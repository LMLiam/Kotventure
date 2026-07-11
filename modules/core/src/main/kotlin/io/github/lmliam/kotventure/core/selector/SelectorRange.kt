package io.github.lmliam.kotventure.core.selector

/**
 * A floating-point range for selector arguments such as `distance`, `pitch`, and `yaw`.
 *
 * Construct open-ended or exact bounds via the helpers [atMost], [atLeast], and [exactly]; for a
 * closed range, pass a native Kotlin range to the consuming argument directly, e.g.
 * `distance(0.5..10.0)`. Validation that differs by argument, such as `distance` rejecting
 * negative or descending bounds while rotations accept both, is applied by the consuming
 * argument. Integer-valued arguments such as `level` and `scores` use the distinct
 * [SelectorIntRange] instead.
 */
@ConsistentCopyVisibility
public data class SelectorRange internal constructor(
    /** Inclusive lower bound, or `null` when the range is open below. */
    public val minimum: Double?,
    /** Inclusive upper bound, or `null` when the range is open above. */
    public val maximum: Double?,
) {
    internal val rendered: String
        get() =
            when {
                minimum != null && minimum == maximum -> formatSelectorNumber(minimum)
                else -> "${minimum.renderedBound()}..${maximum.renderedBound()}"
            }

    /** The vanilla selector-argument rendering (`5`, `1.5..`, `..3`, `1..3.5`). */
    override fun toString(): String = rendered
}

private fun Double?.renderedBound(): String = this?.let(::formatSelectorNumber).orEmpty()

/** A range matching values up to and including [max] (renders as `..max`). */
public fun atMost(max: Double): SelectorRange {
    require(max.isFinite()) { "Range value must be finite, got: $max" }
    return SelectorRange(minimum = null, maximum = max)
}

/** A range matching values at least [min] (renders as `min..`). */
public fun atLeast(min: Double): SelectorRange {
    require(min.isFinite()) { "Range value must be finite, got: $min" }
    return SelectorRange(minimum = min, maximum = null)
}

/** A range matching exactly [value] (renders as `value`). */
public fun exactly(value: Double): SelectorRange {
    require(value.isFinite()) { "Range value must be finite, got: $value" }
    return SelectorRange(minimum = value, maximum = value)
}

internal fun closedRange(
    min: Double,
    max: Double,
): SelectorRange {
    require(min.isFinite()) { "Range min must be finite, got: $min" }
    require(max.isFinite()) { "Range max must be finite, got: $max" }
    return SelectorRange(minimum = min, maximum = max)
}

internal fun SelectorRange.requireAscending(argument: String): SelectorRange {
    require(minimum == null || maximum == null || minimum <= maximum) {
        "Selector argument '$argument' requires min <= max, got: $rendered"
    }
    return this
}

internal fun SelectorRange.requireNonNegative(argument: String): SelectorRange {
    require((minimum ?: 0.0) >= 0.0 && (maximum ?: 0.0) >= 0.0) {
        "Selector argument '$argument' does not accept negative bounds, got: $rendered"
    }
    return this
}
