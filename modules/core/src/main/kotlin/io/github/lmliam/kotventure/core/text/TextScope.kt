package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
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
     * Appends a nested text child configured by [init].
     */
    public fun text(init: TextScope.() -> Unit)
}
