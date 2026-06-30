package io.github.lmliam.kotventure.core.nbt

import kotlin.reflect.KClass

/**
 * Builds a homogeneous NBT list (`TAG_List`) of scalar [values].
 *
 * The element type is fixed by [T], so the list cannot be mixed; `listOf("a", 1)` infers `T = Any`
 * and is rejected. Supported element types are `String`, `Byte`, `Short`, `Int`, `Long`, `Float`,
 * and `Double`. For lists of compounds, use the [listOf] overload that takes an element block.
 *
 * Defined as an extension on [NbtCompoundScope] so it shadows [kotlin.collections.listOf] only
 * inside an `nbt { ... }` block and never leaks into the surrounding namespace.
 *
 * @throws IllegalArgumentException if `T` is not a supported scalar NBT type.
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@Suppress("UnusedReceiverParameter")
public inline fun <reified T> NbtCompoundScope.listOf(vararg values: T): NbtList =
    nbtScalarList(T::class, values.asList())

/**
 * Builds a homogeneous NBT list (`TAG_List`) of compounds built by [init].
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtListSample
 */
@Suppress("UnusedReceiverParameter")
public fun NbtCompoundScope.listOf(init: NbtListScope.() -> Unit): NbtList = NbtListBuilder().apply(init).build()

@PublishedApi
internal fun nbtScalarList(
    type: KClass<*>,
    values: List<Any?>,
): NbtList = NbtList(values.map { scalarElement(type, it) })

private fun scalarElement(
    type: KClass<*>,
    value: Any?,
): NbtValue =
    when (type) {
        String::class -> NbtValue.StringValue(value as String)
        Byte::class -> NbtValue.ByteValue(value as Byte)
        Short::class -> NbtValue.ShortValue(value as Short)
        Int::class -> NbtValue.IntValue(value as Int)
        Long::class -> NbtValue.LongValue(value as Long)
        Float::class -> NbtValue.FloatValue(value as Float)
        Double::class -> NbtValue.DoubleValue(value as Double)
        else -> throw IllegalArgumentException(
            "NBT list elements must be a scalar NBT type (String, Byte, Short, Int, Long, Float, Double); " +
                "use listOf { element { ... } } for compounds. Got: ${type.qualifiedName ?: type}.",
        )
    }
