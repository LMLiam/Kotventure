package io.github.lmliam.kotventure.core.nbt

internal class NbtListBuilder : NbtListScope {
    private val elements = mutableListOf<NbtValue>()

    fun build(): List<NbtValue> = elements.toList()

    override fun byte(value: Byte) {
        elements += NbtValue.ByteValue(value)
    }

    override fun short(value: Short) {
        elements += NbtValue.ShortValue(value)
    }

    override fun int(value: Int) {
        elements += NbtValue.IntValue(value)
    }

    override fun long(value: Long) {
        elements += NbtValue.LongValue(value)
    }

    override fun float(value: Float) {
        elements += NbtValue.FloatValue(value)
    }

    override fun double(value: Double) {
        elements += NbtValue.DoubleValue(value)
    }

    override fun string(value: String) {
        elements += NbtValue.StringValue(value)
    }

    override fun compound(init: NbtCompoundScope.() -> Unit) {
        val nested = NbtCompoundBuilder().apply(init).build()
        elements += NbtValue.CompoundValue(nested)
    }
}
