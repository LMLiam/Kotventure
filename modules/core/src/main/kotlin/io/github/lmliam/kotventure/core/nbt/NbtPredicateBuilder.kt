package io.github.lmliam.kotventure.core.nbt

internal class NbtPredicateBuilder : NbtPredicateScope {
    private val entries = linkedMapOf<String, NbtLiteralValue>()

    fun build(): NbtCompoundPredicate =
        NbtCompoundPredicate(entries.map { (key, value) -> NbtPredicateEntry(key, value) })

    override infix fun String.eq(value: String) {
        entries[this] = NbtLiteralValue.StringValue(value)
    }

    override infix fun String.eq(value: Byte) {
        entries[this] = NbtLiteralValue.ByteValue(value)
    }

    override infix fun String.eq(value: Short) {
        entries[this] = NbtLiteralValue.ShortValue(value)
    }

    override infix fun String.eq(value: Int) {
        entries[this] = NbtLiteralValue.IntValue(value)
    }

    override infix fun String.eq(value: Long) {
        entries[this] = NbtLiteralValue.LongValue(value)
    }

    override infix fun String.eq(value: Float) {
        entries[this] = NbtLiteralValue.FloatValue(value)
    }

    override infix fun String.eq(value: Double) {
        entries[this] = NbtLiteralValue.DoubleValue(value)
    }

    override infix fun String.eq(init: NbtPredicateScope.() -> Unit) {
        val nested = NbtPredicateBuilder().apply(init).build()
        entries[this] = NbtLiteralValue.CompoundValue(nested)
    }
}
