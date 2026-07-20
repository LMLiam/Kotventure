@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.item.fixture

import io.papermc.paper.datacomponent.DataComponentType
import org.bukkit.NamespacedKey

internal class FakeValuedDataComponentType(
    private val key: NamespacedKey,
) : DataComponentType.Valued<Any> {
    override fun getKey(): NamespacedKey = key

    override fun isPersistent(): Boolean = true
}
