package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Scope for building NBT compounds.
 *
 * A compound contains ordered `key eq value` entries. The Kotlin type of the value selects the SNBT literal form.
 * For example, a [Byte] renders as `1b`, and a [Long] renders as `1L`. Use the lambda overload of [eq] to add a nested
 * compound. The array overloads copy their input arrays.
 *
 * Each key can occur one time in a compound. An [eq] call throws [IllegalStateException] if the key already exists.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtCompoundScopeSample
 */
@KotventureDslMarker
public interface NbtCompoundScope {
    /** Sets [this] key to the given string value. */
    public infix fun String.eq(value: String)

    /** Sets [this] key to the given boolean, rendered as a byte (`true` → `1b`, `false` → `0b`). */
    public infix fun String.eq(value: Boolean)

    /** Sets [this] key to the given byte value. For example, `1b` is an SNBT byte. */
    public infix fun String.eq(value: Byte)

    /** Sets [this] key to the given short value. For example, `1s` is an SNBT short. */
    public infix fun String.eq(value: Short)

    /** Sets [this] key to the given int value. For example, `1` is an SNBT integer. */
    public infix fun String.eq(value: Int)

    /** Sets [this] key to the given long value. For example, `1L` is an SNBT long. */
    public infix fun String.eq(value: Long)

    /** Sets [this] key to the given float value. For example, `1.0f` is an SNBT float. */
    public infix fun String.eq(value: Float)

    /** Sets [this] key to the given double value. For example, `1.0d` is an SNBT double. */
    public infix fun String.eq(value: Double)

    /** Sets [this] key to a nested compound built by [init] (renders as `{...}`). */
    public infix fun String.eq(init: NbtCompoundScope.() -> Unit)

    /** Sets [this] key to the given byte array. For example, `[B;1b,2b]` is an SNBT byte array. */
    public infix fun String.eq(values: ByteArray)

    /** Sets [this] key to the given int array. For example, `[I;1,2]` is an SNBT integer array. */
    public infix fun String.eq(values: IntArray)

    /** Sets [this] key to the given long array. For example, `[L;1L,2L]` is an SNBT long array. */
    public infix fun String.eq(values: LongArray)

    /** Sets [this] key to a homogeneous list. For example, `[1,2,3]` is an SNBT list. Build it with [list]. */
    public infix fun String.eq(value: NbtList)
}
