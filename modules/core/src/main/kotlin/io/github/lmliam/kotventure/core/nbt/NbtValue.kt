package io.github.lmliam.kotventure.core.nbt

internal sealed interface NbtValue {
    data class StringValue(
        val value: String,
    ) : NbtValue

    data class ByteValue(
        val value: Byte,
    ) : NbtValue

    data class ShortValue(
        val value: Short,
    ) : NbtValue

    data class IntValue(
        val value: Int,
    ) : NbtValue

    data class LongValue(
        val value: Long,
    ) : NbtValue

    data class FloatValue(
        val value: Float,
    ) : NbtValue

    data class DoubleValue(
        val value: Double,
    ) : NbtValue

    data class CompoundValue(
        val compound: NbtCompound,
    ) : NbtValue

    data class ByteArrayValue(
        val values: ByteArray,
    ) : NbtValue {
        override fun equals(other: Any?): Boolean = other is ByteArrayValue && values.contentEquals(other.values)

        override fun hashCode(): Int = values.contentHashCode()
    }

    data class IntArrayValue(
        val values: IntArray,
    ) : NbtValue {
        override fun equals(other: Any?): Boolean = other is IntArrayValue && values.contentEquals(other.values)

        override fun hashCode(): Int = values.contentHashCode()
    }

    data class LongArrayValue(
        val values: LongArray,
    ) : NbtValue {
        override fun equals(other: Any?): Boolean = other is LongArrayValue && values.contentEquals(other.values)

        override fun hashCode(): Int = values.contentHashCode()
    }

    data class ListValue(
        val elements: List<NbtValue>,
    ) : NbtValue
}
