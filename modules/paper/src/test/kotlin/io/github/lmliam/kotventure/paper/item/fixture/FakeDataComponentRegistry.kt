@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.item.fixture

import io.papermc.paper.datacomponent.DataComponentType
import io.papermc.paper.registry.tag.Tag
import io.papermc.paper.registry.tag.TagKey
import net.kyori.adventure.key.Key
import org.bukkit.NamespacedKey
import org.bukkit.Registry
import java.util.stream.Stream

internal class FakeDataComponentRegistry : Registry<DataComponentType> {
    override fun get(key: NamespacedKey): DataComponentType = type(key)

    override fun getOrThrow(key: Key): DataComponentType = type(NamespacedKey(key.namespace(), key.value()))

    override fun getKey(value: DataComponentType): NamespacedKey = value.key

    override fun hasTag(key: TagKey<DataComponentType>): Boolean = error("not used")

    override fun getTag(key: TagKey<DataComponentType>): Tag<DataComponentType> = error("not used")

    override fun getTags(): Collection<Tag<DataComponentType>> = error("not used")

    override fun stream(): Stream<DataComponentType> = error("not used")

    override fun keyStream(): Stream<NamespacedKey> = error("not used")

    override fun size(): Int = 0

    override fun iterator(): MutableIterator<DataComponentType> = error("not used")

    private fun type(key: NamespacedKey): DataComponentType =
        if (key.key in nonValuedKeys) {
            FakeNonValuedDataComponentType(key)
        } else {
            FakeValuedDataComponentType(key)
        }

    private companion object {
        val nonValuedKeys: Set<String> = setOf("unbreakable", "intangible_projectile", "glider")
    }
}
