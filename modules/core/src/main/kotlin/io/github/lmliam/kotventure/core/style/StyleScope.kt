package io.github.lmliam.kotventure.core.style

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.event.ClickScope
import io.github.lmliam.kotventure.core.event.HoverScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

/**
 * Scope for configuring Adventure style attributes.
 */
@KotventureDslMarker
public interface StyleScope :
    ClickScope,
    HoverScope {
    /**
     * Applies [color] to the style being configured, or clears the color when [color] is null.
     */
    public fun color(color: TextColor?)

    /**
     * Applies [font] to the style being configured, or clears the font when [font] is null.
     */
    public fun font(font: Key?)

    /**
     * Applies [insertion] as shift-click insertion text, or clears it when [insertion] is null.
     */
    public fun insertion(insertion: String?)

    /**
     * Enables [decoration] on the style being configured.
     */
    public fun decorate(decoration: TextDecoration) {
        decoration(decoration, true)
    }

    /**
     * Sets [decoration] to [State.TRUE], [State.FALSE], or [State.NOT_SET] from [flag].
     */
    public fun decoration(
        decoration: TextDecoration,
        flag: Boolean?,
    )

    /**
     * Sets [decoration] to [state].
     */
    public fun decoration(
        decoration: TextDecoration,
        state: State,
    )

    /**
     * Enables bold text on the style being configured.
     */
    public fun bold() {
        bold(true)
    }

    /**
     * Sets bold text to [State.TRUE], [State.FALSE], or [State.NOT_SET] from [flag].
     */
    public fun bold(flag: Boolean?) {
        decoration(TextDecoration.BOLD, flag)
    }

    /**
     * Sets bold text to [state].
     */
    public fun bold(state: State) {
        decoration(TextDecoration.BOLD, state)
    }

    /**
     * Enables italic text on the style being configured.
     */
    public fun italic() {
        italic(true)
    }

    /**
     * Sets italic text to [State.TRUE], [State.FALSE], or [State.NOT_SET] from [flag].
     */
    public fun italic(flag: Boolean?) {
        decoration(TextDecoration.ITALIC, flag)
    }

    /**
     * Sets italic text to [state].
     */
    public fun italic(state: State) {
        decoration(TextDecoration.ITALIC, state)
    }

    /**
     * Enables underlined text on the style being configured.
     */
    public fun underlined() {
        underlined(true)
    }

    /**
     * Sets underlined text to [State.TRUE], [State.FALSE], or [State.NOT_SET] from [flag].
     */
    public fun underlined(flag: Boolean?) {
        decoration(TextDecoration.UNDERLINED, flag)
    }

    /**
     * Sets underlined text to [state].
     */
    public fun underlined(state: State) {
        decoration(TextDecoration.UNDERLINED, state)
    }

    /**
     * Enables strikethrough text on the style being configured.
     */
    public fun strikethrough() {
        strikethrough(true)
    }

    /**
     * Sets strikethrough text to [State.TRUE], [State.FALSE], or [State.NOT_SET] from [flag].
     */
    public fun strikethrough(flag: Boolean?) {
        decoration(TextDecoration.STRIKETHROUGH, flag)
    }

    /**
     * Sets strikethrough text to [state].
     */
    public fun strikethrough(state: State) {
        decoration(TextDecoration.STRIKETHROUGH, state)
    }

    /**
     * Enables obfuscated text on the style being configured.
     */
    public fun obfuscated() {
        obfuscated(true)
    }

    /**
     * Sets obfuscated text to [State.TRUE], [State.FALSE], or [State.NOT_SET] from [flag].
     */
    public fun obfuscated(flag: Boolean?) {
        decoration(TextDecoration.OBFUSCATED, flag)
    }

    /**
     * Sets obfuscated text to [state].
     */
    public fun obfuscated(state: State) {
        decoration(TextDecoration.OBFUSCATED, state)
    }
}
