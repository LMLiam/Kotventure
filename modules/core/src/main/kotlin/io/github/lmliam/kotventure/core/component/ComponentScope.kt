package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.Style

/**
 * Configures the style and ordered children of one Adventure component.
 *
 * The scope inherits component styling from [StyleScope]. Use [append], [newline], and feature extensions to add
 * children in declaration order.
 * Feature extensions include [text][io.github.lmliam.kotventure.core.text.text],
 * [keybind][io.github.lmliam.kotventure.core.keybind.keybind], and
 * [translatable][io.github.lmliam.kotventure.core.translatable.translatable].
 *
 * @sample io.github.lmliam.kotventure.core.component.componentScopeSample
 */
@KotventureDslMarker
public interface ComponentScope : StyleScope {
    /**
     * Replaces the component's complete Adventure style with [style].
     *
     * @throws IllegalStateException when a style is already applied in this block.
     */
    public fun style(style: Style)

    /**
     * Builds and applies one complete Adventure style from [init].
     *
     * @throws IllegalStateException when a style is already applied in this block.
     */
    public fun style(init: StyleScope.() -> Unit)

    /**
     * Appends [component] as the next child without copying it.
     */
    public fun <T : ComponentLike> append(component: T)

    /**
     * Appends an Adventure newline component as the next child.
     */
    public fun newline()
}
