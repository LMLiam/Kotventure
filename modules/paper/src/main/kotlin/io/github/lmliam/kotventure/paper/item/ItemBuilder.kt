package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.text.TextScope
import org.bukkit.inventory.ItemStack

internal class ItemBuilder(
    private val stack: ItemStack,
) : ItemScope {
    override fun name(
        value: String,
        init: TextScope.() -> Unit,
    ) {
        stack.name(value, init)
    }

    override fun lore(init: LoreScope.() -> Unit) {
        stack.lore(init)
    }
}
