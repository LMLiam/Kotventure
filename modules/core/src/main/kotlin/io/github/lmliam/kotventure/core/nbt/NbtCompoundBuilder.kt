package io.github.lmliam.kotventure.core.nbt

internal class NbtCompoundBuilder : NbtCompoundScope {
    private val entries = linkedMapOf<String, NbtValue>()

    fun build(): NbtCompound = NbtCompound(entries.map { (key, value) -> NbtCompoundEntry(key, value) })

    private fun put(
        key: String,
        value: NbtValue,
    ) {
        check(key !in entries) { "NBT key '$key' is already set." }
        entries[key] = value
    }

    override infix fun String.eq(value: String) {
        put(this, NbtValue.StringValue(value))
    }

    override infix fun String.eq(value: Boolean) {
        put(this, NbtValue.ByteValue(if (value) 1 else 0))
    }

    override infix fun String.eq(value: Byte) {
        put(this, NbtValue.ByteValue(value))
    }

    override infix fun String.eq(value: Short) {
        put(this, NbtValue.ShortValue(value))
    }

    override infix fun String.eq(value: Int) {
        put(this, NbtValue.IntValue(value))
    }

    override infix fun String.eq(value: Long) {
        put(this, NbtValue.LongValue(value))
    }

    override infix fun String.eq(value: Float) {
        put(this, NbtValue.FloatValue(value))
    }

    override infix fun String.eq(value: Double) {
        put(this, NbtValue.DoubleValue(value))
    }

    override infix fun String.eq(init: NbtCompoundScope.() -> Unit) {
        put(this, NbtValue.CompoundValue(NbtCompoundBuilder().apply(init).build()))
    }

    override infix fun String.eq(values: ByteArray) {
        put(this, NbtValue.ByteArrayValue(values.copyOf()))
    }

    override infix fun String.eq(values: IntArray) {
        put(this, NbtValue.IntArrayValue(values.copyOf()))
    }

    override infix fun String.eq(values: LongArray) {
        put(this, NbtValue.LongArrayValue(values.copyOf()))
    }

    override infix fun String.eq(value: NbtList) {
        put(this, NbtValue.ListValue(value.elements))
    }
}
