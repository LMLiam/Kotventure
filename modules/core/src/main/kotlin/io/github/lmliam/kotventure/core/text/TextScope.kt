package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.keybind.KeybindScope
import io.github.lmliam.kotventure.core.score.ScoreScope
import io.github.lmliam.kotventure.core.selector.SelectorScope
import io.github.lmliam.kotventure.core.style.StyleScope
import io.github.lmliam.kotventure.core.translatable.TranslatableScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * Scope for configuring a text component and its nested child components.
 */
@KotventureDslMarker
public interface TextScope {
    /**
     * Replaces the text content of the component being configured.
     */
    public fun content(value: String)

    /**
     * Applies a text color to the component being configured.
     */
    public fun color(color: TextColor)

    /**
     * Applies a complete Adventure style to the component being configured.
     */
    public fun style(style: Style)

    /**
     * Applies style attributes from [init] to the component being configured.
     */
    public fun style(init: StyleScope.() -> Unit)

    /**
     * Enables [decoration] on the component being configured.
     */
    public fun decorate(decoration: TextDecoration)

    /**
     * Enables bold text on the component being configured.
     */
    public fun bold()

    /**
     * Enables italic text on the component being configured.
     */
    public fun italic()

    /**
     * Enables underlined text on the component being configured.
     */
    public fun underlined()

    /**
     * Enables strikethrough text on the component being configured.
     */
    public fun strikethrough()

    /**
     * Enables obfuscated text on the component being configured.
     */
    public fun obfuscated()

    /**
     * Appends a nested text child with [value] as its initial content and configured by [init].
     */
    public fun text(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Appends an existing Adventure [Component] as a child of the component being configured.
     */
    public fun append(component: Component)

    /**
     * Appends a nested translatable child with [key] as its translation key.
     */
    public fun translatable(
        key: String,
        init: TranslatableScope.() -> Unit = {},
    )

    /**
     * Appends a nested keybind child with [keybind] as its keybind identifier.
     */
    public fun keybind(
        keybind: String,
        init: KeybindScope.() -> Unit = {},
    )

    /**
     * Appends a nested score child with [name] and [objective].
     */
    public fun score(
        name: String,
        objective: String,
        init: ScoreScope.() -> Unit = {},
    )

    /**
     * Appends a nested selector child with [pattern] as its selector pattern.
     */
    public fun selector(
        pattern: String,
        init: SelectorScope.() -> Unit = {},
    )

    /**
     * Appends an Adventure newline component as a child of the component being configured.
     */
    public fun newline()

    /**
     * Appends a nested text child configured by [init].
     */
    public fun text(init: TextScope.() -> Unit)
}
