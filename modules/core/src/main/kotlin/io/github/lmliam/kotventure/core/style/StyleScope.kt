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
 * Configures the style of a component or reusable style value.
 *
 * The available attributes are colour, shadow, font, insertion, decorations, and the event behaviour from [ClickScope]
 * and [HoverScope]. Each attribute and each named decoration is write-once in one block.
 *
 * **Decorations.** Each named decoration (`bold`, `italic`, `underlined`, `strikethrough`,
 * `obfuscated`) and the generic [decoration]/[decorate] APIs share the same three forms and the
 * same once-assign rule:
 * - Call with no argument to enable, for example `bold()`.
 * - Call with a nullable flag, for example `bold(false)` or `bold(null)`. These calls use [State.TRUE], [State.FALSE],
 *   or [State.NOT_SET].
 * - Call with an explicit [State], for example `bold(State.TRUE)`.
 *
 * A duplicate assignment throws [IllegalStateException]. The exception message identifies the duplicate attribute.
 *
 * @sample io.github.lmliam.kotventure.core.style.styleScopeSample
 */
@KotventureDslMarker
public interface StyleScope :
    ClickScope,
    HoverScope {
    /**
     * Sets [color], or explicitly clears inherited colour when [color] is `null`.
     *
     * @throws IllegalStateException when the colour is already set in this block.
     */
    public fun color(color: TextColor?)

    /**
     * Sets shadow [color], or explicitly clears inherited shadow colour when [color] is `null`.
     *
     * @throws IllegalStateException when shadow colour is already set in this block.
     */
    public fun shadow(color: ShadowColor?)

    /**
     * Sets a shadow from [color] and [alpha]. The default alpha is fully opaque.
     *
     * [alpha] must be in `0..255`. Kotventure passes the value to Adventure without additional validation.
     *
     * @throws IllegalStateException when shadow colour is already set in this block.
     */
    public fun shadow(
        color: TextColor,
        alpha: Int = 0xFF,
    ) {
        shadow(ShadowColor.shadowColor(color, alpha))
    }

    /**
     * Sets [font], or explicitly clears the inherited font when [font] is `null`.
     *
     * @throws IllegalStateException when the font is already set in this block.
     */
    public fun font(font: Key?)

    /**
     * Sets shift-click [insertion] text, or explicitly clears it when [insertion] is `null`.
     *
     * @throws IllegalStateException when insertion text is already set in this block.
     */
    public fun insertion(insertion: String?)

    /**
     * Enables [decoration] ([State.TRUE]).
     *
     * @throws IllegalStateException when [decoration] is already set in this block.
     */
    public fun decorate(decoration: TextDecoration) {
        decoration(decoration, true)
    }

    /**
     * Sets [decoration] from [flag] (`true` / `false` / `null` → TRUE / FALSE / NOT_SET).
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
     * Enables [TextDecoration.BOLD].
     *
     * @throws IllegalStateException when bold is already set in this block.
     */
    public fun bold() {
        bold(true)
    }

    /**
     * Sets bold to `TRUE`, `FALSE`, or `NOT_SET` for a `true`, `false`, or `null` [flag].
     *
     * @throws IllegalStateException when bold is already set in this block.
     */
    public fun bold(flag: Boolean?) {
        decoration(TextDecoration.BOLD, flag)
    }

    /**
     * Sets bold to [state].
     *
     * @throws IllegalStateException when bold is already set in this block.
     */
    public fun bold(state: State) {
        decoration(TextDecoration.BOLD, state)
    }

    /**
     * Enables [TextDecoration.ITALIC].
     *
     * @throws IllegalStateException when italic is already set in this block.
     */
    public fun italic() {
        italic(true)
    }

    /**
     * Sets italic to `TRUE`, `FALSE`, or `NOT_SET` for a `true`, `false`, or `null` [flag].
     *
     * @throws IllegalStateException when italic is already set in this block.
     */
    public fun italic(flag: Boolean?) {
        decoration(TextDecoration.ITALIC, flag)
    }

    /**
     * Sets italic to [state].
     *
     * @throws IllegalStateException when italic is already set in this block.
     */
    public fun italic(state: State) {
        decoration(TextDecoration.ITALIC, state)
    }

    /**
     * Enables [TextDecoration.UNDERLINED].
     *
     * @throws IllegalStateException when underlined is already set in this block.
     */
    public fun underlined() {
        underlined(true)
    }

    /**
     * Sets underlined to `TRUE`, `FALSE`, or `NOT_SET` for a `true`, `false`, or `null` [flag].
     *
     * @throws IllegalStateException when underlined is already set in this block.
     */
    public fun underlined(flag: Boolean?) {
        decoration(TextDecoration.UNDERLINED, flag)
    }

    /**
     * Sets underlined to [state].
     *
     * @throws IllegalStateException when underlined is already set in this block.
     */
    public fun underlined(state: State) {
        decoration(TextDecoration.UNDERLINED, state)
    }

    /**
     * Enables [TextDecoration.STRIKETHROUGH].
     *
     * @throws IllegalStateException when strikethrough is already set in this block.
     */
    public fun strikethrough() {
        strikethrough(true)
    }

    /**
     * Sets strikethrough to `TRUE`, `FALSE`, or `NOT_SET` for a `true`, `false`, or `null` [flag].
     *
     * @throws IllegalStateException when strikethrough is already set in this block.
     */
    public fun strikethrough(flag: Boolean?) {
        decoration(TextDecoration.STRIKETHROUGH, flag)
    }

    /**
     * Sets strikethrough to [state].
     *
     * @throws IllegalStateException when strikethrough is already set in this block.
     */
    public fun strikethrough(state: State) {
        decoration(TextDecoration.STRIKETHROUGH, state)
    }

    /**
     * Enables [TextDecoration.OBFUSCATED].
     *
     * @throws IllegalStateException when obfuscated is already set in this block.
     */
    public fun obfuscated() {
        obfuscated(true)
    }

    /**
     * Sets obfuscated to `TRUE`, `FALSE`, or `NOT_SET` for a `true`, `false`, or `null` [flag].
     *
     * @throws IllegalStateException when obfuscated is already set in this block.
     */
    public fun obfuscated(flag: Boolean?) {
        decoration(TextDecoration.OBFUSCATED, flag)
    }

    /**
     * Sets obfuscated to [state].
     *
     * @throws IllegalStateException when obfuscated is already set in this block.
     */
    public fun obfuscated(state: State) {
        decoration(TextDecoration.OBFUSCATED, state)
    }
}
