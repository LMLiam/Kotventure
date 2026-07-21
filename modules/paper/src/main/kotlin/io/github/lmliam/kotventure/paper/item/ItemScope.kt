package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope

/**
 * Configures the custom name and lore of a newly created item stack.
 *
 * Functions in this scope modify the stack that [item] creates.
 */
@KotventureDslMarker
public interface ItemScope {
    /**
     * Replaces the custom name with the styled literal [value].
     *
     * The name is non-italic unless [init] sets the italic state.
     */
    public fun name(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Replaces all lore with the lines from [init].
 *
     * Calls in [init] preserve their order. Each line other than a [LoreScope.blank] line is
     * non-italic unless that line sets the italic state.
     */
    public fun lore(init: LoreScope.() -> Unit)
}
