package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.ComponentLike

/**
 * Configures the separator, style, and children of a selector component.
 */
@KotventureDslMarker
public interface SelectorScope : ComponentScope {
    /**
     * Sets the component that separates selected entity names.
     *
     * @throws IllegalStateException when the separator is already set in this block.
     */
    public fun separator(separator: ComponentLike)

    /**
     * Builds an inline text component and sets it as the separator between selected entity names.
     *
     * @throws IllegalStateException when the separator is already set in this block.
     */
    public fun separator(init: TextScope.() -> Unit)
}
