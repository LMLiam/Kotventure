package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.text.TextScope
import io.github.lmliam.kotventure.core.text.text
import org.bukkit.inventory.meta.ItemMeta

/**
 * Replaces this metadata's custom name with styled literal [value].
 *
 * The resulting component is explicitly non-italic unless [init] sets an italic state.
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
 * Replaces this metadata's lore with the lines accumulated by [init].
 *
 * Each non-empty line is explicitly non-italic unless that line sets an italic state.
 *
 * @sample io.github.lmliam.kotventure.paper.item.editItemMetaSample
 */
public fun ItemMeta.lore(init: LoreScope.() -> Unit) {
    lore(LoreBuilder().apply(init).build())
}
