package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.ComponentLike

/**
 * Scope for configuring a selector component with a separator, style, and children.
 */
@KotventureDslMarker
public interface SelectorScope : ComponentScope {
    /**
     * Applies [separator] between selected entity names.
     */
    public fun separator(separator: ComponentLike)

    /**
     * Builds and applies an inline text separator between selected entity names.
     */
    public fun separator(init: TextScope.() -> Unit)
}
