package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.text.TextScope
import io.github.lmliam.kotventure.core.text.text
import org.bukkit.inventory.meta.ItemMeta

/**
 * Replaces the custom name in this metadata with the styled literal [value].
 *
 * The function modifies this [ItemMeta]. It does not apply detached metadata to an item stack.
 * The name is non-italic unless [init] sets the italic state.
 *
 * @sample io.github.lmliam.kotventure.paper.item.editItemMetaSample
 */
public fun ItemMeta.name(
    value: String,
    init: TextScope.() -> Unit = {},
) {
    customName(text(value, init).nonItalicByDefault())
}

/**
 * Replaces the lore in this metadata with the lines from [init].
 *
 * The function modifies this [ItemMeta]. It does not apply detached metadata to an item stack.
 * Calls in [init] preserve their order. Each line other than a [LoreScope.blank] line is
 * non-italic unless that line sets the italic state.
 *
 * @sample io.github.lmliam.kotventure.paper.item.editItemMetaSample
 */
public fun ItemMeta.lore(init: LoreScope.() -> Unit) {
    lore(LoreBuilder().apply(init).build())
}
