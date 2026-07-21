@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.item

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * Creates an [ItemStack] with [material] and [amount].
 *
 * The [init] block modifies the custom name and lore of the new stack. This function does not add
 * the stack to an inventory and does not display it.
 *
 * @throws IllegalArgumentException when [material] is not an item or [amount] is less than one.
 * @return the new and configured item stack.
 * @sample io.github.lmliam.kotventure.paper.item.itemSample
 */
public fun item(
    material: Material,
    amount: Int = 1,
    init: ItemScope.() -> Unit,
): ItemStack = ItemStack.of(material, amount).also { ItemBuilder(it).apply(init) }
