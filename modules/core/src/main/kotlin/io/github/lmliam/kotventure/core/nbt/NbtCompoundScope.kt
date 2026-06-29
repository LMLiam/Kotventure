package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Scope for building NBT compounds.
 *
 * A compound is a set of `key eq value` assertions; the value's Kotlin type selects the SNBT
 * literal form (e.g. a [Byte] renders as `1b`, a [Long] as `1L`). Nest a compound with the lambda
 * overload of [eq]:
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtCompoundScopeSample
 */
@KotventureDslMarker
public interface NbtCompoundScope {
    /** Sets [this] key to the given string value. */
    public infix fun String.eq(value: String)

    /** Sets [this] key to the given byte value (renders as SNBT byte, e.g. `1b`). */
    public infix fun String.eq(value: Byte)

    /** Sets [this] key to the given short value (renders as SNBT short, e.g. `1s`). */
    public infix fun String.eq(value: Short)

    /** Sets [this] key to the given int value (renders as SNBT int, e.g. `1`). */
    public infix fun String.eq(value: Int)

    /** Sets [this] key to the given long value (renders as SNBT long, e.g. `1L`). */
    public infix fun String.eq(value: Long)

    /** Sets [this] key to the given float value (renders as SNBT float, e.g. `1.0f`). */
    public infix fun String.eq(value: Float)

    /** Sets [this] key to the given double value (renders as SNBT double, e.g. `1.0d`). */
    public infix fun String.eq(value: Double)

    /** Sets [this] key to a nested compound built by [init] (renders as `{...}`). */
    public infix fun String.eq(init: NbtCompoundScope.() -> Unit)

    /** Sets [this] key to the given byte array (renders as SNBT byte array, e.g. `[B;1b,2b]`). */
    public infix fun String.eq(values: ByteArray)

    /** Sets [this] key to the given int array (renders as SNBT int array, e.g. `[I;1,2]`). */
    public infix fun String.eq(values: IntArray)

    /** Sets [this] key to the given long array (renders as SNBT long array, e.g. `[L;1L,2L]`). */
    public infix fun String.eq(values: LongArray)
}
