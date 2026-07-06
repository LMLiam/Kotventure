package io.github.lmliam.kotventure.core.nbt

/**
 * Builds a homogeneous NBT list (`TAG_List`) of [values].
 *
 * The element type is fixed by [T], so a well-typed call (`list(1, 2, 3)`, `list("a", "b")`)
 * produces a list of one kind. Mixing element types (`list("a", 1)`) compiles — [T] simply
 * widens to a common supertype — but is rejected at runtime with a descriptive error.
 *
 * Supported element types: the scalars (`String`, `Boolean`, `Byte`, `Short`, `Int`, `Long`,
 * `Float`, `Double`), the arrays (`ByteArray`, `IntArray`, `LongArray`), and nested lists
 * ([NbtList]) — so `list(list(1, 2), list(3, 4))` and `list(intArrayOf(1), intArrayOf(2))`
 * are lists of lists and lists of arrays. `Boolean` renders as a byte (`true` -> `1b`).
 * For lists of compounds, use the [list] overload that takes compound blocks.
 *
 * Defined as an extension on [NbtCompoundScope] so it stays scoped to an `nbt { ... }` block.
 *
 * @throws IllegalArgumentException if any element is null or not a supported NBT element type.
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@Suppress("UnusedReceiverParameter")
public fun <T> NbtCompoundScope.list(vararg values: T): NbtList = NbtList(values.map { it.toNbtValue() })

/** Iterable overload for ergonomics. */
@Suppress("UnusedReceiverParameter")
public fun <T> NbtCompoundScope.list(values: Iterable<T>): NbtList = NbtList(values.map { it.toNbtValue() })

/**
 * Builds an empty NBT list (`TAG_List` with no elements): `"Items" eq list()`.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@Suppress("UnusedReceiverParameter")
public fun NbtCompoundScope.list(): NbtList = NbtList(emptyList())

/**
 * Builds a homogeneous NBT list (`TAG_List`) of the compounds built by [blocks].
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@Suppress("UnusedReceiverParameter")
@JvmName("listOfCompounds")
public fun NbtCompoundScope.list(vararg blocks: NbtCompoundScope.() -> Unit): NbtList =
    NbtList(blocks.map(::buildNbtCompound))

/** Build a compound and wrap as a compound value. Put this next to your nbt DSL if you prefer. */
internal fun buildNbtCompound(block: NbtCompoundScope.() -> Unit): NbtValue.CompoundValue =
    NbtValue.CompoundValue(NbtCompoundBuilder().apply(block).build())

private fun Any?.toNbtValue(): NbtValue =
    when (this) {
        is String -> NbtValue.StringValue(this)
        is Boolean -> NbtValue.ByteValue(if (this) 1.toByte() else 0.toByte())
        is Byte -> NbtValue.ByteValue(this)
        is Short -> NbtValue.ShortValue(this)
        is Int -> NbtValue.IntValue(this)
        is Long -> NbtValue.LongValue(this)
        is Float -> NbtValue.FloatValue(this)
        is Double -> NbtValue.DoubleValue(this)
        is ByteArray -> NbtValue.ByteArrayValue(copyOf())
        is IntArray -> NbtValue.IntArrayValue(copyOf())
        is LongArray -> NbtValue.LongArrayValue(copyOf())
        is NbtList -> NbtValue.ListValue(elements)
        null -> throw IllegalArgumentException("NBT list elements cannot be null.")
        else -> throw IllegalArgumentException(
            "NBT list elements must be a scalar (String, Boolean, Byte, Short, Int, Long, Float, Double), " +
                    "an array (ByteArray, IntArray, LongArray), a nested list, or a compound block. " +
                    "Got: ${this::class.qualifiedName ?: this::class}.",
        )
    }
