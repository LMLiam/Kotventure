package io.github.lmliam.kotventure.core.style

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.event.ClickScope
import io.github.lmliam.kotventure.core.event.HoverScope
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

/**
 * Configures a component's or reusable style's visual attributes: color, shadow, font, insertion,
 * decorations, and (via [ClickScope]/[HoverScope]) click and hover behaviour.
 *
 * Each decoration offers three forms — a no-arg enable (`bold()`), a nullable-flag setter that can also clear
 * it (`bold(false)`, `bold(null)`), and an explicit [State] setter — so styles can both set and unset
 * attributes when composed.
 *
 * @sample io.github.lmliam.kotventure.core.style.styleScopeSample
 */
@KotventureDslMarker
public interface StyleScope :
    ClickScope,
    HoverScope {
    /**
     * Applies [color] to the style being configured, or clears the color when [color] is null.
     *
     * @throws IllegalStateException when the color is already set in this block.
     */
    public fun color(color: TextColor?)

    /**
     * Applies [color] as the shadow color of the style being configured, or clears it when [color] is null.
     *
     * @throws IllegalStateException when the shadow color is already set in this block.
     */
    public fun shadow(color: ShadowColor?)

    /**
     * Applies [color] as a shadow color with [alpha] opacity, where [alpha] is in the `0..255` range and defaults to
     * fully opaque.
     *
     * @throws IllegalStateException when the shadow color is already set in this block.
     */
    public fun shadow(
        color: TextColor,
        alpha: Int = 0xFF,
    ) {
        shadow(ShadowColor.shadowColor(color, alpha))
    }

    /**
     * Applies [font] to the style being configured, or clears the font when [font] is null.
     *
     * @throws IllegalStateException when the font is already set in this block.
     */
    public fun font(font: Key?)

    /**
     * Applies [insertion] as shift-click insertion text, or clears it when [insertion] is null.
     *
     * @throws IllegalStateException when the insertion is already set in this block.
     */
    public fun insertion(insertion: String?)

    /**
     * Enables [decoration] on the style being configured.
     *
     * @throws IllegalStateException when [decoration] is already set in this block.
     */
    public fun decorate(decoration: TextDecoration) {
        decoration(decoration, true)
    }

    /**
     * Sets [decoration] to [State.TRUE], [State.FALSE], or [State.NOT_SET] from [flag].
     *
     * @throws IllegalStateException when [decoration] is already set in this block.
     */
    public fun decoration(
        decoration: TextDecoration,
        flag: Boolean?,
    )

    /**
     * Sets [decoration] to [state].
     *
     * @throws IllegalStateException when [decoration] is already set in this block.
     */
    public fun decoration(
        decoration: TextDecoration,
        state: State,
    )

    /**
     * Enables bold text on the style being configured.
     *
     * @throws IllegalStateException when bold is already set in this block.
     */
    public fun bold() {
        bold(true)
    }

    /**
     * Sets bold text to [State.TRUE], [State.FALSE], or [State.NOT_SET] from [flag].
     *
     * @throws IllegalStateException when bold is already set in this block.
     */
    public fun bold(flag: Boolean?) {
        decoration(TextDecoration.BOLD, flag)
    }

    /**
     * Sets bold text to [state].
     *
     * @throws IllegalStateException when bold is already set in this block.
     */
    public fun bold(state: State) {
        decoration(TextDecoration.BOLD, state)
    }

    /**
     * Enables italic text on the style being configured.
     *
     * @throws IllegalStateException when italic is already set in this block.
     */
    public fun italic() {
        italic(true)
    }

    /**
     * Sets italic text to [State.TRUE], [State.FALSE], or [State.NOT_SET] from [flag].
     *
     * @throws IllegalStateException when italic is already set in this block.
     */
    public fun italic(flag: Boolean?) {
        decoration(TextDecoration.ITALIC, flag)
    }

    /**
     * Sets italic text to [state].
     *
     * @throws IllegalStateException when italic is already set in this block.
     */
    public fun italic(state: State) {
        decoration(TextDecoration.ITALIC, state)
    }

    /**
     * Enables underlined text on the style being configured.
     *
     * @throws IllegalStateException when underlined is already set in this block.
     */
    public fun underlined() {
        underlined(true)
    }

    /**
     * Sets underlined text to [State.TRUE], [State.FALSE], or [State.NOT_SET] from [flag].
     *
     * @throws IllegalStateException when underlined is already set in this block.
     */
    public fun underlined(flag: Boolean?) {
        decoration(TextDecoration.UNDERLINED, flag)
    }

    /**
     * Sets underlined text to [state].
     *
     * @throws IllegalStateException when underlined is already set in this block.
     */
    public fun underlined(state: State) {
        decoration(TextDecoration.UNDERLINED, state)
    }

    /**
     * Enables strikethrough text on the style being configured.
     *
     * @throws IllegalStateException when strikethrough is already set in this block.
     */
    public fun strikethrough() {
        strikethrough(true)
    }

    /**
     * Sets strikethrough text to [State.TRUE], [State.FALSE], or [State.NOT_SET] from [flag].
     *
     * @throws IllegalStateException when strikethrough is already set in this block.
     */
    public fun strikethrough(flag: Boolean?) {
        decoration(TextDecoration.STRIKETHROUGH, flag)
    }

    /**
     * Sets strikethrough text to [state].
     *
     * @throws IllegalStateException when strikethrough is already set in this block.
     */
    public fun strikethrough(state: State) {
        decoration(TextDecoration.STRIKETHROUGH, state)
    }

    /**
     * Enables obfuscated text on the style being configured.
     *
     * @throws IllegalStateException when obfuscated is already set in this block.
     */
    public fun obfuscated() {
        obfuscated(true)
    }

    /**
     * Sets obfuscated text to [State.TRUE], [State.FALSE], or [State.NOT_SET] from [flag].
     *
     * @throws IllegalStateException when obfuscated is already set in this block.
     */
    public fun obfuscated(flag: Boolean?) {
        decoration(TextDecoration.OBFUSCATED, flag)
    }

    /**
     * Sets obfuscated text to [state].
     *
     * @throws IllegalStateException when obfuscated is already set in this block.
     */
    public fun obfuscated(state: State) {
        decoration(TextDecoration.OBFUSCATED, state)
    }
}
