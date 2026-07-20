package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope

/**
 * Configures the text data components of a newly created item.
 */
@KotventureDslMarker
public interface ItemScope {
    /**
     * Replaces the item's custom name with styled literal [value].
     *
     * The resulting component is explicitly non-italic unless [init] sets an italic state.
     */
    public fun name(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Replaces the item's lore with the lines accumulated by [init].
     *
     * Each non-empty line is explicitly non-italic unless that line sets an italic state.
     */
    public fun lore(init: LoreScope.() -> Unit)
}
