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
 * Configures the visual attributes of a component or reusable style. The attributes are colour, shadow, font,
 * insertion, decorations, and the click and hover behaviour from [ClickScope] and [HoverScope].
 *
 * **Decorations.** Each named decoration (`bold`, `italic`, `underlined`, `strikethrough`,
 * `obfuscated`) and the generic [decoration]/[decorate] APIs share the same three forms and the
 * same once-assign rule:
 * - no-arg enable — e.g. `bold()`
 * - nullable flag — e.g. `bold(false)`, `bold(null)` → [State.TRUE] / [State.FALSE] / [State.NOT_SET]
 * - explicit [State] — e.g. `bold(State.TRUE)`
 *
 * Setting the same attribute twice in one block throws [IllegalStateException] (named in the
 * message). That contract applies to every setter below unless a method documents otherwise.
 *
 * @sample io.github.lmliam.kotventure.core.style.styleScopeSample
 */
@KotventureDslMarker
public interface StyleScope :
    ClickScope,
    HoverScope {
    /**
     * Applies [color], or clears it when [color] is null.
     *
 * @throws IllegalStateException when the colour is already set in this block.
     */
    public fun color(color: TextColor?)

    /**
     * Applies shadow [color], or clears it when [color] is null.
     *
     * @throws IllegalStateException when already set in this block.
     */
    public fun shadow(color: ShadowColor?)

    /**
     * Applies a shadow colour with [alpha] in `0..255`. The default value is fully opaque.
     *
     * @throws IllegalStateException when shadow is already set in this block.
     */
    public fun shadow(
        color: TextColor,
        alpha: Int = 0xFF,
    ) {
        shadow(ShadowColor.shadowColor(color, alpha))
    }

    /**
     * Applies [font], or clears it when [font] is null.
     *
     * @throws IllegalStateException when already set in this block.
     */
    public fun font(font: Key?)

    /**
     * Applies shift-click [insertion] text, or clears it when [insertion] is null.
     *
     * @throws IllegalStateException when already set in this block.
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

    /** Enables [TextDecoration.BOLD]. @throws IllegalStateException when already set. */
    public fun bold() {
        bold(true)
    }

    /** Sets [TextDecoration.BOLD] from [flag]. @throws IllegalStateException when already set. */
    public fun bold(flag: Boolean?) {
        decoration(TextDecoration.BOLD, flag)
    }

    /** Sets [TextDecoration.BOLD] to [state]. @throws IllegalStateException when already set. */
    public fun bold(state: State) {
        decoration(TextDecoration.BOLD, state)
    }

    /** Enables [TextDecoration.ITALIC]. @throws IllegalStateException when already set. */
    public fun italic() {
        italic(true)
    }

    /** Sets [TextDecoration.ITALIC] from [flag]. @throws IllegalStateException when already set. */
    public fun italic(flag: Boolean?) {
        decoration(TextDecoration.ITALIC, flag)
    }

    /** Sets [TextDecoration.ITALIC] to [state]. @throws IllegalStateException when already set. */
    public fun italic(state: State) {
        decoration(TextDecoration.ITALIC, state)
    }

    /** Enables [TextDecoration.UNDERLINED]. @throws IllegalStateException when already set. */
    public fun underlined() {
        underlined(true)
    }

    /** Sets [TextDecoration.UNDERLINED] from [flag]. @throws IllegalStateException when already set. */
    public fun underlined(flag: Boolean?) {
        decoration(TextDecoration.UNDERLINED, flag)
    }

    /** Sets [TextDecoration.UNDERLINED] to [state]. @throws IllegalStateException when already set. */
    public fun underlined(state: State) {
        decoration(TextDecoration.UNDERLINED, state)
    }

    /** Enables [TextDecoration.STRIKETHROUGH]. @throws IllegalStateException when already set. */
    public fun strikethrough() {
        strikethrough(true)
    }

    /** Sets [TextDecoration.STRIKETHROUGH] from [flag]. @throws IllegalStateException when already set. */
    public fun strikethrough(flag: Boolean?) {
        decoration(TextDecoration.STRIKETHROUGH, flag)
    }

    /** Sets [TextDecoration.STRIKETHROUGH] to [state]. @throws IllegalStateException when already set. */
    public fun strikethrough(state: State) {
        decoration(TextDecoration.STRIKETHROUGH, state)
    }

    /** Enables [TextDecoration.OBFUSCATED]. @throws IllegalStateException when already set. */
    public fun obfuscated() {
        obfuscated(true)
    }

    /** Sets [TextDecoration.OBFUSCATED] from [flag]. @throws IllegalStateException when already set. */
    public fun obfuscated(flag: Boolean?) {
        decoration(TextDecoration.OBFUSCATED, flag)
    }

    /** Sets [TextDecoration.OBFUSCATED] to [state]. @throws IllegalStateException when already set. */
    public fun obfuscated(state: State) {
        decoration(TextDecoration.OBFUSCATED, state)
    }
}
