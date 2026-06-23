package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style

/**
 * Scope for configuring behavior shared by every Adventure component builder.
 */
@KotventureDslMarker
public interface ComponentScope : StyleScope {
    /**
     * Applies a complete Adventure style to the component being configured.
     */
    public fun style(style: Style)

    /**
     * Applies style attributes from [init] to the component being configured.
     */
    public fun style(init: StyleScope.() -> Unit)

    /**
     * Appends an existing Adventure [Component] as a child of the component being configured.
     */
    public fun append(component: Component)

    /**
     * Appends an Adventure newline component as a child of the component being configured.
     */
    public fun newline()
}
