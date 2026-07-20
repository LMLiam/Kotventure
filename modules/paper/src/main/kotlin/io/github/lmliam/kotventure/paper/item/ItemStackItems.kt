@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.text.TextScope
import io.github.lmliam.kotventure.core.text.text
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.inventory.ItemStack

/**
 * Replaces this item's custom-name data component with styled literal [value].
 *
 * The resulting component is explicitly non-italic unless [init] sets an italic state.
 *
 * @sample io.github.lmliam.kotventure.paper.item.itemSample
 */
public fun ItemStack.name(
    value: String,
    init: TextScope.() -> Unit = {},
) {
    setData(DataComponentTypes.CUSTOM_NAME, text(value, init).nonItalicByDefault())
}

/**
 * Replaces this item's lore data component with the lines accumulated by [init].
 *
 * Each non-empty line is explicitly non-italic unless that line sets an italic state.
 *
 * @sample io.github.lmliam.kotventure.paper.item.itemSample
 */
public fun ItemStack.lore(init: LoreScope.() -> Unit) {
    setData(DataComponentTypes.LORE, ItemLore.lore(LoreBuilder().apply(init).build()))
}
