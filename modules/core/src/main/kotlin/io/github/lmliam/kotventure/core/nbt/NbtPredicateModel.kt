package io.github.lmliam.kotventure.core.nbt

internal data class NbtCompoundPredicate(
    val entries: List<NbtPredicateEntry>,
)

internal data class NbtPredicateEntry(
    val key: String,
    val value: NbtLiteralValue,
)

internal sealed interface NbtLiteralValue {
    data class StringValue(
        val value: String,
    ) : NbtLiteralValue

    data class ByteValue(
        val value: Byte,
    ) : NbtLiteralValue

    data class ShortValue(
        val value: Short,
    ) : NbtLiteralValue

    data class IntValue(
        val value: Int,
    ) : NbtLiteralValue

    data class LongValue(
        val value: Long,
    ) : NbtLiteralValue

    data class FloatValue(
        val value: Float,
    ) : NbtLiteralValue

    data class DoubleValue(
        val value: Double,
    ) : NbtLiteralValue

    data class CompoundValue(
        val predicate: NbtCompoundPredicate,
    ) : NbtLiteralValue
}
