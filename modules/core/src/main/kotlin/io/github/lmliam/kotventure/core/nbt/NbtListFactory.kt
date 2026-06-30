package io.github.lmliam.kotventure.core.nbt

import kotlin.reflect.KClass

/**
 * Builds a homogeneous NBT list (`TAG_List`) of [values].
 *
 * The element type is fixed by [T], so the list cannot be mixed; `list("a", 1)` infers `T = Any` and is
 * rejected. Supported element types: the scalars (`String`, `Boolean`, `Byte`, `Short`, `Int`, `Long`,
 * `Float`, `Double`), the arrays (`ByteArray`, `IntArray`, `LongArray`), and nested lists ([NbtList]) —
 * so `list(list(1, 2), list(3, 4))` and `list(intArrayOf(1), intArrayOf(2))` are lists of lists and
 * lists of arrays. `Boolean` renders as a byte (`true` → `1b`). For lists of compounds, use the [list]
 * overload that takes compound blocks.
 *
 * Defined as an extension on [NbtCompoundScope] so it stays scoped to an `nbt { ... }` block.
 *
 * @throws IllegalArgumentException if [T] is not a supported NBT element type.
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@Suppress("UnusedReceiverParameter")
public inline fun <reified T> NbtCompoundScope.list(vararg values: T): NbtList = nbtList(T::class, values.asList())

/**
 * Builds a homogeneous NBT list (`TAG_List`) of the compounds built by [compounds].
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@Suppress("UnusedReceiverParameter")
public fun NbtCompoundScope.list(vararg compounds: NbtCompoundScope.() -> Unit): NbtList =
    NbtList(compounds.map { NbtValue.CompoundValue(NbtCompoundBuilder().apply(it).build()) })

@PublishedApi
internal fun nbtList(
    type: KClass<*>,
    values: List<Any?>,
): NbtList = NbtList(values.map { nbtElement(type, it) })

private fun nbtElement(
    type: KClass<*>,
    value: Any?,
): NbtValue =
    when (type) {
        String::class -> NbtValue.StringValue(value as String)
        Boolean::class -> NbtValue.ByteValue(if (value as Boolean) 1 else 0)
        Byte::class -> NbtValue.ByteValue(value as Byte)
        Short::class -> NbtValue.ShortValue(value as Short)
        Int::class -> NbtValue.IntValue(value as Int)
        Long::class -> NbtValue.LongValue(value as Long)
        Float::class -> NbtValue.FloatValue(value as Float)
        Double::class -> NbtValue.DoubleValue(value as Double)
        ByteArray::class -> NbtValue.ByteArrayValue((value as ByteArray).copyOf())
        IntArray::class -> NbtValue.IntArrayValue((value as IntArray).copyOf())
        LongArray::class -> NbtValue.LongArrayValue((value as LongArray).copyOf())
        NbtList::class -> NbtValue.ListValue((value as NbtList).elements)
        else -> throw IllegalArgumentException(
            "NBT list elements must be a scalar (String, Boolean, Byte, Short, Int, Long, Float, Double), " +
                "an array (ByteArray, IntArray, LongArray), a nested list, or a compound block. " +
                "Got: ${type.qualifiedName ?: type}.",
        )
    }
