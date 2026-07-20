package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.ComponentLike

/**
 * Accumulates item lore lines in call order.
 */
@KotventureDslMarker
public interface LoreScope {
    /**
     * Adds this string as a plain, explicitly non-italic lore line.
     */
    public operator fun String.unaryPlus()

    /**
     * Adds this string as a styled lore line configured by [init].
     *
     * The line is explicitly non-italic unless [init] sets an italic state.
     */
    public operator fun String.invoke(init: TextScope.() -> Unit)

    /**
     * Adds this component-like value as a lore line, explicitly non-italic unless it sets an italic state.
     */
    public operator fun ComponentLike.unaryPlus()

    /**
     * Adds an empty spacer line.
     */
    public fun blank()
}
