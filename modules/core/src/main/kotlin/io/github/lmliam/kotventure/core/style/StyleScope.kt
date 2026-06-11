package io.github.lmliam.kotventure.core.style

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State
import java.time.temporal.TemporalAmount
import io.github.lmliam.kotventure.core.event.callback as callbackEvent
import io.github.lmliam.kotventure.core.event.changePage as changePageEvent
import io.github.lmliam.kotventure.core.event.copy as copyEvent
import io.github.lmliam.kotventure.core.event.copyToClipboard as copyToClipboardEvent
import io.github.lmliam.kotventure.core.event.open as openEvent
import io.github.lmliam.kotventure.core.event.openFile as openFileEvent
import io.github.lmliam.kotventure.core.event.openUrl as openUrlEvent
import io.github.lmliam.kotventure.core.event.run as runEvent
import io.github.lmliam.kotventure.core.event.runCommand as runCommandEvent
import io.github.lmliam.kotventure.core.event.suggest as suggestEvent
import io.github.lmliam.kotventure.core.event.suggestCommand as suggestCommandEvent
import kotlin.time.Duration as KotlinDuration

/**
 * Scope for configuring Adventure style attributes.
 */
@KotventureDslMarker
public interface StyleScope {
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
     * Applies [event] as the click event, or clears the click event when [event] is null.
     */
    public fun clickEvent(event: ClickEvent<*>?)

    /**
     * Applies [event] as the click event, or clears the click event when [event] is null.
     */
    public fun click(event: ClickEvent<*>?) {
        clickEvent(event)
    }

    /**
     * Applies a click event that opens [url].
     */
    public fun open(url: String) {
        click(openEvent(url))
    }

    /**
     * Applies a click event that opens [url].
     */
    public fun openUrl(url: String) {
        click(openUrlEvent(url))
    }

    /**
     * Applies a click event that opens a local [file] path.
     */
    public fun openFile(file: String) {
        click(openFileEvent(file))
    }

    /**
     * Applies a click event that runs [command].
     */
    public fun run(command: String) {
        click(runEvent(command))
    }

    /**
     * Applies a click event that runs [command].
     */
    public fun runCommand(command: String) {
        click(runCommandEvent(command))
    }

    /**
     * Applies a click event that suggests [command] in chat.
     */
    public fun suggest(command: String) {
        click(suggestEvent(command))
    }

    /**
     * Applies a click event that suggests [command] in chat.
     */
    public fun suggestCommand(command: String) {
        click(suggestCommandEvent(command))
    }

    /**
     * Applies a click event that changes a book to [page].
     */
    public fun changePage(page: Int) {
        click(changePageEvent(page))
    }

    /**
     * Applies a click event that copies [text] to the clipboard.
     */
    public fun copy(text: String) {
        click(copyEvent(text))
    }

    /**
     * Applies a click event that copies [text] to the clipboard.
     */
    public fun copyToClipboard(text: String) {
        click(copyToClipboardEvent(text))
    }

    /**
     * Applies a server-side callback click event from [function].
     */
    public fun callback(function: ClickCallback<Audience>) {
        click(callbackEvent(function))
    }

    /**
     * Applies a server-side callback click event from [function] with [uses] and [lifetime].
     */
    public fun callback(
        uses: Int,
        lifetime: TemporalAmount,
        function: ClickCallback<Audience>,
    ) {
        click(callbackEvent(uses, lifetime, function))
    }

    /**
     * Applies a server-side callback click event from [function] with [uses] and [lifetime].
     */
    public fun callback(
        uses: Int,
        lifetime: KotlinDuration,
        function: ClickCallback<Audience>,
    ) {
        click(callbackEvent(uses, lifetime, function))
    }

    /**
     * Applies a server-side callback click event from [function] with prebuilt [options].
     */
    public fun callback(
        options: ClickCallback.Options,
        function: ClickCallback<Audience>,
    ) {
        click(callbackEvent(options, function))
    }

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
