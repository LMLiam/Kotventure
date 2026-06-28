package io.github.lmliam.kotventure.core.nbt

internal class NbtPredicateBuilder : NbtPredicateScope {
    private val entries = linkedMapOf<String, NbtLiteralValue>()

    fun addEntry(
        key: String,
        value: NbtLiteralValue,
    ) {
        entries[key] = value
    }

    fun build(): NbtCompoundPredicate =
        NbtCompoundPredicate(entries.map { (key, value) -> NbtPredicateEntry(key, value) })

    override fun key(name: String): NbtPredicateKey = NbtPredicateKey(name)

    override infix fun NbtPredicateKey.eq(value: String) {
        this@NbtPredicateBuilder.addEntry(name, NbtLiteralValue.StringValue(value))
    }

    override infix fun NbtPredicateKey.eq(value: Byte) {
        this@NbtPredicateBuilder.addEntry(name, NbtLiteralValue.ByteValue(value))
    }

    override infix fun NbtPredicateKey.eq(value: Short) {
        this@NbtPredicateBuilder.addEntry(name, NbtLiteralValue.ShortValue(value))
    }

    override infix fun NbtPredicateKey.eq(value: Int) {
        this@NbtPredicateBuilder.addEntry(name, NbtLiteralValue.IntValue(value))
    }

    override infix fun NbtPredicateKey.eq(value: Long) {
        this@NbtPredicateBuilder.addEntry(name, NbtLiteralValue.LongValue(value))
    }

    override infix fun NbtPredicateKey.eq(value: Float) {
        this@NbtPredicateBuilder.addEntry(name, NbtLiteralValue.FloatValue(value))
    }

    override infix fun NbtPredicateKey.eq(value: Double) {
        this@NbtPredicateBuilder.addEntry(name, NbtLiteralValue.DoubleValue(value))
    }

    override infix fun NbtPredicateKey.eq(value: NbtLiteral) {
        this@NbtPredicateBuilder.addEntry(name, value.value)
    }

    override fun nbtByte(value: Byte): NbtLiteral = NbtLiteral(NbtLiteralValue.ByteValue(value))

    override fun nbtShort(value: Short): NbtLiteral = NbtLiteral(NbtLiteralValue.ShortValue(value))

    override fun nbtInt(value: Int): NbtLiteral = NbtLiteral(NbtLiteralValue.IntValue(value))

    override fun nbtLong(value: Long): NbtLiteral = NbtLiteral(NbtLiteralValue.LongValue(value))

    override fun nbtFloat(value: Float): NbtLiteral = NbtLiteral(NbtLiteralValue.FloatValue(value))

    override fun nbtDouble(value: Double): NbtLiteral = NbtLiteral(NbtLiteralValue.DoubleValue(value))

    override fun nbtByteArray(vararg values: Byte): NbtLiteral = NbtLiteral(NbtLiteralValue.ByteArrayValue(values))

    override fun nbtIntArray(vararg values: Int): NbtLiteral = NbtLiteral(NbtLiteralValue.IntArrayValue(values))

    override fun nbtLongArray(vararg values: Long): NbtLiteral = NbtLiteral(NbtLiteralValue.LongArrayValue(values))

    override fun nbtString(value: String): NbtLiteral = NbtLiteral(NbtLiteralValue.StringValue(value))

    override fun nbtCompound(init: NbtPredicateScope.() -> Unit): NbtLiteral {
        val nested = NbtPredicateBuilder()
        nested.init()
        return NbtLiteral(NbtLiteralValue.CompoundValue(nested.build()))
    }

    override fun nbtList(vararg values: NbtLiteral): NbtLiteral =
        NbtLiteral(NbtLiteralValue.ListValue(values.map { it.value }))
}
