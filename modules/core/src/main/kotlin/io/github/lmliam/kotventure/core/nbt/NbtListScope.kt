package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Scope for building a heterogeneous NBT list.
 *
 * Elements are added in order; the resulting list renders as `[element, element, ...]` in SNBT.
 */
@KotventureDslMarker
public interface NbtListScope {
    /** Adds a byte element (renders as `1b`). */
    public fun byte(value: Byte)

    /** Adds a short element (renders as `1s`). */
    public fun short(value: Short)

    /** Adds an int element. */
    public fun int(value: Int)

    /** Adds a long element (renders as `1L`). */
    public fun long(value: Long)

    /** Adds a float element (renders as `1.0f`). */
    public fun float(value: Float)

    /** Adds a double element (renders as `1.0d`). */
    public fun double(value: Double)

    /** Adds a string element. */
    public fun string(value: String)

    /** Adds a compound element built by [init]. */
    public fun compound(init: NbtCompoundScope.() -> Unit)
}
