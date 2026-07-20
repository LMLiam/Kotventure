@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.item.fixture

import io.papermc.paper.datacomponent.DataComponentType
import org.bukkit.NamespacedKey

internal class FakeNonValuedDataComponentType(
    private val key: NamespacedKey,
) : DataComponentType.NonValued {
    override fun getKey(): NamespacedKey = key

    override fun isPersistent(): Boolean = true
}
