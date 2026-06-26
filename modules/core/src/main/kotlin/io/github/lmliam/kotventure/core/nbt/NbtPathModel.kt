package io.github.lmliam.kotventure.core.nbt

internal sealed interface NbtPathNode {
    data class Key(
        val name: String,
    ) : NbtPathNode

    data class Index(
        val index: Int,
    ) : NbtPathNode

    data object AllElements : NbtPathNode

    data class MatchingElements(
        val predicate: NbtCompoundPredicate,
    ) : NbtPathNode
}

internal sealed interface NbtPathRepr {
    data class Structured(
        val nodes: List<NbtPathNode>,
    ) : NbtPathRepr

    data class Raw(
        val path: String,
    ) : NbtPathRepr
}

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

    data class CompoundValue(
        val predicate: NbtCompoundPredicate,
    ) : NbtLiteralValue

    data class ListValue(
        val elements: List<NbtLiteralValue>,
    ) : NbtLiteralValue
}
