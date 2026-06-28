package io.github.lmliam.kotventure.core.component

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.Style

/**
 * The receiver inside a component-building block (such as `component { }` or `text { }`).
 *
 * It adds children — via [append], [newline], and the feature extensions like
 * [text][io.github.lmliam.kotventure.core.text.text], [keybind][io.github.lmliam.kotventure.core.keybind.keybind], and
 * [translatable][io.github.lmliam.kotventure.core.translatable.translatable] — on top of the styling it inherits from
 * [StyleScope], so a component's own style and its
 * children are configured in one place.
 *
 * @sample io.github.lmliam.kotventure.core.component.componentScopeSample
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
     * Appends an existing Adventure [ComponentLike] as a child of the component being configured.
     */
    public fun <T : ComponentLike> append(component: T)

    /**
     * Appends an Adventure newline component as a child of the component being configured.
     */
    public fun newline()
}
