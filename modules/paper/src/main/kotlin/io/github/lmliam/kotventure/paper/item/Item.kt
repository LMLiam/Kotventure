@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.item

import org.bukkit.Material
import org.bukkit.inventory.ItemStack

/**
 * Creates an [ItemStack] of [material] and [amount], then configures its text through [init].
 *
 * Construction only — the item is not added to an inventory or otherwise displayed.
 *
 * @throws IllegalArgumentException when [material] is not an item or [amount] is less than one.
 * @sample io.github.lmliam.kotventure.paper.item.itemSample
 */
public fun item(
    material: Material,
    amount: Int = 1,
    init: ItemScope.() -> Unit,
): ItemStack = ItemStack.of(material, amount).also { ItemBuilder(it).apply(init) }
