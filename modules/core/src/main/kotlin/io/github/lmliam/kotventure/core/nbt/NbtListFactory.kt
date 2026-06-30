package io.github.lmliam.kotventure.core.nbt

/**
 * Builds a homogeneous NBT list (`TAG_List`) of [values].
 *
 * The element type is fixed by [T], so the list cannot be mixed; `list("a", 1)` infers `T` to a common
 * supertype and is rejected at compile time. Supported element types: the scalars (`String`, `Boolean`,
 * `Byte`, `Short`, `Int`, `Long`, `Float`, `Double`), the arrays (`ByteArray`, `IntArray`, `LongArray`),
 * and nested lists ([NbtList]) — so `list(list(1, 2), list(3, 4))` and `list(intArrayOf(1), intArrayOf(2))`
 * are lists of lists and lists of arrays. `Boolean` renders as a byte (`true` → `1b`). For lists of
 * compounds, use the [list] overload that takes compound blocks.
 *
 * Defined as an extension on [NbtCompoundScope] so it stays scoped to an `nbt { ... }` block.
 *
 * @throws IllegalArgumentException if [T] is not a supported NBT element type.
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@Suppress("UnusedReceiverParameter")
public inline fun <reified T> NbtCompoundScope.list(vararg values: T): NbtList = nbtList(values.asList())

/**
 * Builds a homogeneous NBT list (`TAG_List`) of the compounds built by [compounds].
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@Suppress("UnusedReceiverParameter")
public fun NbtCompoundScope.list(vararg compounds: NbtCompoundScope.() -> Unit): NbtList =
    NbtList(compounds.map { NbtValue.CompoundValue(NbtCompoundBuilder().apply(it).build()) })

@PublishedApi
internal fun nbtList(values: List<Any?>): NbtList = NbtList(values.map(::nbtElement))

private fun nbtElement(value: Any?): NbtValue =
    when (value) {
        is String -> NbtValue.StringValue(value)
        is Boolean -> NbtValue.ByteValue(if (value) 1 else 0)
        is Byte -> NbtValue.ByteValue(value)
        is Short -> NbtValue.ShortValue(value)
        is Int -> NbtValue.IntValue(value)
        is Long -> NbtValue.LongValue(value)
        is Float -> NbtValue.FloatValue(value)
        is Double -> NbtValue.DoubleValue(value)
        is ByteArray -> NbtValue.ByteArrayValue(value.copyOf())
        is IntArray -> NbtValue.IntArrayValue(value.copyOf())
        is LongArray -> NbtValue.LongArrayValue(value.copyOf())
        is NbtList -> NbtValue.ListValue(value.elements)
        else -> throw IllegalArgumentException(
            "NBT list elements must be a scalar (String, Boolean, Byte, Short, Int, Long, Float, Double), " +
                    "an array (ByteArray, IntArray, LongArray), a nested list, or a compound block. " +
                    "Got: ${if (value == null) "null" else value::class.qualifiedName ?: value::class}.",
        )
    }
