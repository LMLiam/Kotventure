package io.github.lmliam.kotventure.core.nbt

internal class NbtCompoundBuilder : NbtCompoundScope {
    private val entries = linkedMapOf<String, NbtValue>()

    fun build(): NbtCompound = NbtCompound(entries.map { (key, value) -> NbtCompoundEntry(key, value) })

    override infix fun String.eq(value: String) {
        entries[this] = NbtValue.StringValue(value)
    }

    override infix fun String.eq(value: Byte) {
        entries[this] = NbtValue.ByteValue(value)
    }

    override infix fun String.eq(value: Short) {
        entries[this] = NbtValue.ShortValue(value)
    }

    override infix fun String.eq(value: Int) {
        entries[this] = NbtValue.IntValue(value)
    }

    override infix fun String.eq(value: Long) {
        entries[this] = NbtValue.LongValue(value)
    }

    override infix fun String.eq(value: Float) {
        entries[this] = NbtValue.FloatValue(value)
    }

    override infix fun String.eq(value: Double) {
        entries[this] = NbtValue.DoubleValue(value)
    }

    override infix fun String.eq(init: NbtCompoundScope.() -> Unit) {
        val nested = NbtCompoundBuilder().apply(init).build()
        entries[this] = NbtValue.CompoundValue(nested)
    }

    override infix fun String.eq(values: ByteArray) {
        entries[this] = NbtValue.ByteArrayValue(values.copyOf())
    }

    override infix fun String.eq(values: IntArray) {
        entries[this] = NbtValue.IntArrayValue(values.copyOf())
    }

    override infix fun String.eq(values: LongArray) {
        entries[this] = NbtValue.LongArrayValue(values.copyOf())
    }

    override infix fun String.eq(value: NbtList) {
        entries[this] = NbtValue.ListValue(value.elements)
    }
}
