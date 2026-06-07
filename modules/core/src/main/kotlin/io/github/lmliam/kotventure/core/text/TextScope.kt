package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.dsl.AdventureDsl
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor

/**
 * Scope for configuring a text component and its nested child components.
 */
@AdventureDsl
public interface TextScope {
    /**
     * Replaces the text content of the component being configured.
     */
    public fun content(value: String): Unit

    /**
     * Applies a text color to the component being configured.
     */
    public fun color(color: TextColor): Unit

    /**
     * Applies a complete Adventure style to the component being configured.
     */
    public fun style(style: Style): Unit

    /**
     * Appends a nested text child configured by [init].
     */
    public fun text(init: TextScope.() -> Unit): Unit
}
