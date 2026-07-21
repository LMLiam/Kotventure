package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.ComponentLike

/**
 * Builds the complete lore list for an item.
 *
 * Each call adds one line. The scope preserves call order. Item functions replace existing lore
 * with the completed list.
 */
@KotventureDslMarker
public interface LoreScope {
    /**
     * Adds the receiver string as a plain, non-italic lore line.
     */
    public operator fun String.unaryPlus()

    /**
     * Adds the receiver string as a styled lore line configured by [init].
     *
     * The line is non-italic unless [init] sets the italic state.
     */
    public operator fun String.invoke(init: TextScope.() -> Unit)

    /**
     * Adds the receiver component-like value as a lore line.
     *
     * The line is non-italic unless the component has an italic state.
     */
    public operator fun ComponentLike.unaryPlus()

    /**
     * Adds an empty spacer line without an explicit italic state.
     */
    public fun blank()
}
