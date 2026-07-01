package io.github.lmliam.kotventure.core.selector

/**
 * An integer-valued range for selector arguments such as `level` and scoreboard objectives.
 *
 * Construct open-ended or exact bounds via [atMost], [atLeast], [exactly]; for a closed range, pass
 * a native Kotlin [IntRange] to the consuming selector argument. Validation that differs by
 * argument is applied when the range is consumed. Integral ranges are distinct from the
 * floating-point [SelectorRange], so fractional values are compile errors.
 */
public class LevelRange internal constructor(
    /** Inclusive lower bound, or `null` when the range is open below. */
    public val minimum: Int?,
    /** Inclusive upper bound, or `null` when the range is open above. */
    public val maximum: Int?,
    internal val rendered: String,
) {
    public override fun equals(other: Any?): Boolean = other is LevelRange && rendered == other.rendered

    public override fun hashCode(): Int = rendered.hashCode()

    public override fun toString(): String = rendered
}

/** A range matching integer values up to and including [max] (renders as `..max`). */
public fun atMost(max: Int): LevelRange =
    LevelRange(
        minimum = null,
        maximum = max,
        rendered = "..$max",
    )

/** A range matching integer values of at least [min] (renders as `min..`). */
public fun atLeast(min: Int): LevelRange =
    LevelRange(
        minimum = min,
        maximum = null,
        rendered = "$min..",
    )

/** A range matching exactly [value] (renders as `value`). */
public fun exactly(value: Int): LevelRange =
    LevelRange(
        minimum = value,
        maximum = value,
        rendered = "$value",
    )

internal fun closedLevelRange(
    min: Int,
    max: Int,
): LevelRange {
    require(min <= max) { "Range min ($min) must not exceed max ($max)" }
    return LevelRange(
        minimum = min,
        maximum = max,
        rendered = "$min..$max",
    )
}
