@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.text.TextScope
import io.github.lmliam.kotventure.core.text.text
import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemLore
import org.bukkit.inventory.ItemStack

/**
 * Replaces the custom-name data component of this stack with the styled literal [value].
 *
 * The function modifies this stack in place. The name is non-italic unless [init] sets the italic
 * state.
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
 * Replaces the lore data component of this stack with the lines from [init].
 *
 * The function modifies this stack in place. Calls in [init] preserve their order. Each line other
 * than a [LoreScope.blank] line is non-italic unless that line sets the italic state.
 *
 * @sample io.github.lmliam.kotventure.paper.item.itemSample
 */
public fun ItemStack.lore(init: LoreScope.() -> Unit) {
    setData(DataComponentTypes.LORE, ItemLore.lore(LoreBuilder().apply(init).build()))
}
