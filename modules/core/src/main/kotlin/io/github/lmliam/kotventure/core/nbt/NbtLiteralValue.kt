package io.github.lmliam.kotventure.core.nbt

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

    data class ByteArrayValue(
        val values: ByteArray,
    ) : NbtLiteralValue {
        override fun equals(other: Any?): Boolean = other is ByteArrayValue && values.contentEquals(other.values)

        override fun hashCode(): Int = values.contentHashCode()
    }

    data class IntArrayValue(
        val values: IntArray,
    ) : NbtLiteralValue {
        override fun equals(other: Any?): Boolean = other is IntArrayValue && values.contentEquals(other.values)

        override fun hashCode(): Int = values.contentHashCode()
    }

    data class LongArrayValue(
        val values: LongArray,
    ) : NbtLiteralValue {
        override fun equals(other: Any?): Boolean = other is LongArrayValue && values.contentEquals(other.values)

        override fun hashCode(): Int = values.contentHashCode()
    }
}
