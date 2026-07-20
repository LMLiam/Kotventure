package io.github.lmliam.kotventure.core.selector

/**
 * An integer range for selector arguments such as `level` and `scores` objectives.
 *
 * Use [atMost], [atLeast], or [exactly] to construct open-ended or exact bounds. For a closed range, give a Kotlin
 * [IntRange] directly to the applicable argument, for example `level(5..30)`.
 * The consuming argument applies its validation. For example, `level` rejects negative bounds, but score objectives
 * accept them. Floating-point arguments such as `distance` use
 * [SelectorRange]. Thus, the compiler rejects fractional values here.
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

    /** The vanilla selector-argument rendering (`5`, `1..`, `..3`, `1..3`). */
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
