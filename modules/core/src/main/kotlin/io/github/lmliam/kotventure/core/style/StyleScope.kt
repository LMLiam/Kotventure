package io.github.lmliam.kotventure.core.style

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * Scope for configuring an Adventure style inline.
 */
@KotventureDslMarker
public interface StyleScope {
    /**
     * Applies a text color to the style being configured.
     */
    public fun color(color: TextColor)

    /**
     * Enables [decoration] on the style being configured.
     */
    public fun decorate(decoration: TextDecoration)

    /**
     * Enables bold text on the style being configured.
     */
    public fun bold()

    /**
     * Enables italic text on the style being configured.
     */
    public fun italic()

    /**
     * Enables underlined text on the style being configured.
     */
    public fun underlined()

    /**
     * Enables strikethrough text on the style being configured.
     */
    public fun strikethrough()

    /**
     * Enables obfuscated text on the style being configured.
     */
    public fun obfuscated()
}
