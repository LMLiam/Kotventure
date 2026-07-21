package io.github.lmliam.kotventure.core.nbt

/**
 * Creates an NBT list (`TAG_List`) from [values].
 *
 * Minecraft requires all list elements to have the same NBT type. This function does not enforce that rule when
 * [values] contain different supported Kotlin types. Supply values that map to one NBT type.
 *
 * Supported elements are strings, booleans, numeric scalar types, [ByteArray], [IntArray], [LongArray], and nested
 * [NbtList] values. A boolean renders as a byte. The function copies input arrays. Use the compound-block overload of
 * [list] for a list of compounds.
 *
 * @throws IllegalArgumentException when an element is null or has an unsupported type.
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@Suppress("UnusedReceiverParameter")
public fun <T> NbtCompoundScope.list(vararg values: T): NbtList = NbtList(values.map { it.toNbtValue() })

/**
 * Creates an NBT list from [values].
 *
 * This overload has the same type, copy, and validation rules as [list].
 *
 * @throws IllegalArgumentException when an element is null or has an unsupported type.
 */
@Suppress("UnusedReceiverParameter")
public fun <T> NbtCompoundScope.list(values: Iterable<T>): NbtList = NbtList(values.map { it.toNbtValue() })

/**
 * Creates an empty NBT list (`TAG_List` with no elements).
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@Suppress("UnusedReceiverParameter")
public fun NbtCompoundScope.list(): NbtList = NbtList(emptyList())

/**
 * Creates an NBT list of the compounds that [blocks] build.
 *
 * The list keeps the order of [blocks]. A block throws [IllegalStateException] if it sets a duplicate key.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@Suppress("UnusedReceiverParameter")
@JvmName("listOfCompounds")
public fun NbtCompoundScope.list(vararg blocks: NbtCompoundScope.() -> Unit): NbtList =
    NbtList(blocks.map(::buildNbtCompound))

/** Creates one internal compound value from [block]. */
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
