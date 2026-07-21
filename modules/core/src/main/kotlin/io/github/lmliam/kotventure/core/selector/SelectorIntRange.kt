package io.github.lmliam.kotventure.core.selector

/**
 * An integer range for selector arguments such as `level` and `scores` objectives.
 *
 * Use [atMost], [atLeast], or [exactly] to create open-ended or exact bounds. Give a Kotlin [IntRange] directly to an
 * applicable argument to create a closed range, for example `level(5..30)`. The consuming argument validates the
 * range. For example, `level` rejects negative bounds, but score objectives accept them.
 *
 * The range is immutable. [toString] returns the canonical vanilla form. Floating-point arguments use
 * [SelectorRange].
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

    /** Returns the canonical vanilla range form, such as `5`, `1..`, `..3`, or `1..3`. */
    override fun toString(): String = rendered
}

private fun Int?.renderedBound(): String = this?.toString().orEmpty()

/** Returns a range that matches integer values up to and including [max]. */
public fun atMost(max: Int): SelectorIntRange = SelectorIntRange(minimum = null, maximum = max)

/** Returns a range that matches integer values greater than or equal to [min]. */
public fun atLeast(min: Int): SelectorIntRange = SelectorIntRange(minimum = min, maximum = null)

/** Returns a range that matches exactly [value]. */
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
