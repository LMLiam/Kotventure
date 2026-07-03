package io.github.lmliam.kotventure.core.selector

/**
 * An integer range for selector arguments such as `level` and `scores` objectives.
 *
 * Construct open-ended or exact bounds via [atMost], [atLeast], [exactly]; for a closed range,
 * pass a native Kotlin [IntRange] to the consuming argument directly, e.g. `level(5..30)`.
 * Validation that differs by argument, such as `level` rejecting negative bounds while score
 * objectives accept them, is applied by the consuming argument. Floating-point arguments such as
 * `distance` use the distinct [SelectorRange], so fractional values here are compile errors rather
 * than invalid selectors.
 */
@ConsistentCopyVisibility
public data class SelectorIntRange internal constructor(
    /** Inclusive lower bound, or `null` when the range is open below. */
    public val minimum: Int?,
    /** Inclusive upper bound, or `null` when the range is open above. */
    public val maximum: Int?,
) {
    internal val rendered: String
        get() =
            when {
                minimum != null && minimum == maximum -> minimum.toString()
                else -> "${minimum.renderedBound()}..${maximum.renderedBound()}"
            }

    override fun toString(): String = rendered
}

private fun Int?.renderedBound(): String = this?.toString().orEmpty()

/** A range matching integer values up to and including [max] (renders as `..max`). */
public fun atMost(max: Int): SelectorIntRange = SelectorIntRange(minimum = null, maximum = max)

/** A range matching integer values of at least [min] (renders as `min..`). */
public fun atLeast(min: Int): SelectorIntRange = SelectorIntRange(minimum = min, maximum = null)

/** A range matching exactly [value] (renders as `value`). */
public fun exactly(value: Int): SelectorIntRange = SelectorIntRange(minimum = value, maximum = value)

internal fun closedRange(range: IntRange): SelectorIntRange {
    require(!range.isEmpty()) { "Range must not be empty, got: $range" }
    return SelectorIntRange(minimum = range.first, maximum = range.last)
}

internal fun SelectorIntRange.requireNonNegative(argument: String): SelectorIntRange {
    require((minimum ?: 0) >= 0 && (maximum ?: 0) >= 0) {
        "Selector argument '$argument' does not accept negative bounds, got: $rendered"
    }
    return this
}
