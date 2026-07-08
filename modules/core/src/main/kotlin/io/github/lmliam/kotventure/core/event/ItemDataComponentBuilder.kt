package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.nbt.NbtCompoundScope
import io.github.lmliam.kotventure.core.nbt.nbt
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.event.DataComponentValue

internal class ItemDataComponentBuilder : ItemDataComponentScope {
    private val components = LinkedHashMap<Key, DataComponentValue>()

    private fun put(
        key: Key,
        value: DataComponentValue,
    ) {
        check(key !in components) { "Data component '$key' is already declared." }
        components[key] = value
    }

    override fun component(
        key: Key,
        init: NbtCompoundScope.() -> Unit,
    ) {
        put(key, nbt(init))
    }

    override fun component(
        key: Key,
        value: DataComponentValue,
    ) {
        put(key, value)
    }

    override fun removed(key: Key) {
        put(key, DataComponentValue.removed())
    }

    internal fun build(): Map<Key, DataComponentValue> = components.toMap()
}
