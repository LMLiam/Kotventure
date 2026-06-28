package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Scope for building NBT compound predicates.
 *
 * ```kotlin
 * matching {
 *     key("id") eq "minecraft:diamond"
 *     key("Count") eq nbtByte(1)
 * }
 * ```
 */
@KotventureDslMarker
public interface NbtPredicateScope {
    /**
     * References a key in the compound tag for matching.
     */
    public fun key(name: String): NbtPredicateKey

    /**
     * Asserts that this key equals the given string value.
     */
    public infix fun NbtPredicateKey.eq(value: String)

    /**
     * Asserts that this key equals the given byte value (renders as SNBT byte, e.g. `1b`).
     */
    public infix fun NbtPredicateKey.eq(value: Byte)

    /**
     * Asserts that this key equals the given short value (renders as SNBT short, e.g. `1s`).
     */
    public infix fun NbtPredicateKey.eq(value: Short)

    /**
     * Asserts that this key equals the given int value (renders as SNBT int, e.g. `1`).
     */
    public infix fun NbtPredicateKey.eq(value: Int)

    /**
     * Asserts that this key equals the given long value (renders as SNBT long, e.g. `1L`).
     */
    public infix fun NbtPredicateKey.eq(value: Long)

    /**
     * Asserts that this key equals the given float value (renders as SNBT float, e.g. `1.0f`).
     */
    public infix fun NbtPredicateKey.eq(value: Float)

    /**
     * Asserts that this key equals the given double value (renders as SNBT double, e.g. `1.0d`).
     */
    public infix fun NbtPredicateKey.eq(value: Double)

    /**
     * Asserts that this key equals the given typed literal.
     */
    public infix fun NbtPredicateKey.eq(value: NbtLiteral)

    /** Creates a byte literal (e.g. `1b` in SNBT). */
    public fun nbtByte(value: Byte): NbtLiteral

    /** Creates a short literal (e.g. `1s` in SNBT). */
    public fun nbtShort(value: Short): NbtLiteral

    /** Creates an int literal (e.g. `1` in SNBT). */
    public fun nbtInt(value: Int): NbtLiteral

    /** Creates a long literal (e.g. `1L` in SNBT). */
    public fun nbtLong(value: Long): NbtLiteral

    /** Creates a float literal (e.g. `1.0f` in SNBT). */
    public fun nbtFloat(value: Float): NbtLiteral

    /** Creates a double literal (e.g. `1.0d` in SNBT). */
    public fun nbtDouble(value: Double): NbtLiteral

    /** Creates a byte array literal (e.g. `[B;1b,2b]` in SNBT). */
    public fun nbtByteArray(vararg values: Byte): NbtLiteral

    /** Creates an int array literal (e.g. `[I;1,2]` in SNBT). */
    public fun nbtIntArray(vararg values: Int): NbtLiteral

    /** Creates a long array literal (e.g. `[L;1L,2L]` in SNBT). */
    public fun nbtLongArray(vararg values: Long): NbtLiteral

    /** Creates a string literal (e.g. `"value"` in SNBT). */
    public fun nbtString(value: String): NbtLiteral

    /** Creates a compound literal (nested predicate). */
    public fun nbtCompound(init: NbtPredicateScope.() -> Unit): NbtLiteral

    /** Creates a list literal (e.g. `[1,2,3]` in SNBT). */
    public fun nbtList(vararg values: NbtLiteral): NbtLiteral
}
