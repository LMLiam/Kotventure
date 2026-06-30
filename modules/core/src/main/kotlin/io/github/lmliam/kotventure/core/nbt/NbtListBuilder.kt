package io.github.lmliam.kotventure.core.nbt

internal class NbtListBuilder : NbtListScope {
    private val elements = mutableListOf<NbtValue>()

    override fun element(init: NbtCompoundScope.() -> Unit) {
        elements += NbtValue.CompoundValue(NbtCompoundBuilder().apply(init).build())
    }

    fun build(): NbtList = NbtList(elements.toList())
}
