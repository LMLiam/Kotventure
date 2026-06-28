package io.github.lmliam.kotventure.core.selector

/**
 * A range for the integer-valued experience `level` selector argument.
 *
 * Construct open-ended or exact bounds via [atMost], [atLeast], [exactly]; for a closed range, pass
 * a native Kotlin [IntRange] to `level(a..b)` directly. Levels are integers, so this is kept
 * distinct from the floating-point [SelectorRange] used by `distance` — `level(exactly(1.5))` is a
 * compile error rather than an invalid selector.
 */
@JvmInline
public value class LevelRange internal constructor(
    internal val rendered: String,
) {
    override fun toString(): String = rendered
}

/** A range matching levels up to and including [max] (renders as `..max`). */
public fun atMost(max: Int): LevelRange = LevelRange("..$max")

/** A range matching levels of at least [min] (renders as `min..`). */
public fun atLeast(min: Int): LevelRange = LevelRange("$min..")

/** A range matching exactly [value] (renders as `value`). */
public fun exactly(value: Int): LevelRange = LevelRange("$value")
