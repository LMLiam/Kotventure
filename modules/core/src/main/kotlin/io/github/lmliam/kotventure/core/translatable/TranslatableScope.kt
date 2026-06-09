package io.github.lmliam.kotventure.core.translatable

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.style.StyleScope
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * Scope for configuring a translatable component with fallback text, arguments, style, and children.
 */
@KotventureDslMarker
public interface TranslatableScope {
    /**
     * Applies fallback text for clients that cannot resolve the translation key.
     */
    public fun fallback(fallback: String)

    /**
     * Appends a component-like translation argument.
     */
    public fun arg(value: ComponentLike)

    /**
     * Appends a boolean translation argument.
     */
    public fun arg(value: Boolean)

    /**
     * Appends a numeric translation argument.
     */
    public fun arg(value: Number)

    /**
     * Appends multiple component-like translation arguments in declaration order.
     */
    public fun args(vararg values: ComponentLike)

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
     * Appends an existing Adventure [Component] as a child of the component being configured.
     */
    public fun append(component: Component)
}
