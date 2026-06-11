package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import kotlin.time.toJavaDuration
import kotlin.time.Duration as KotlinDuration

/**
 * Scope for applying Adventure click events.
 */
@KotventureDslMarker
public interface ClickScope {
    /**
     * Applies [event] as the click event, or clears the click event when [event] is null.
     */
    public fun click(event: ClickEvent<*>?)

    /**
     * Applies an Adventure click event built from a typed [action] and [payload].
     *
     * @throws IllegalArgumentException when Adventure rejects the action/payload pair.
     */
    public fun <P : ClickEvent.Payload> click(
        action: ClickEvent.Action<P>,
        payload: P,
    ) {
        click(ClickEvent.clickEvent(action, payload))
    }

    /**
     * Applies a click event that opens [target].
     *
     * File URI targets are converted to open-file events; all other targets are treated as URLs.
     */
    public fun open(target: String) {
        click(openTarget(target))
    }

    /**
     * Applies a click event that opens [url].
     */
    public fun openUrl(url: String) {
        click(ClickEvent.openUrl(url))
    }

    /**
     * Applies a click event that opens a local [file] path.
     */
    public fun openFile(file: String) {
        click(ClickEvent.openFile(file))
    }

    /**
     * Applies a click event that runs [command].
     */
    public fun run(command: String) {
        click(ClickEvent.runCommand(command))
    }

    /**
     * Applies a click event that suggests [command] in chat.
     */
    public fun suggest(command: String) {
        click(ClickEvent.suggestCommand(command))
    }

    /**
     * Applies a click event that changes a book to [page].
     */
    public fun changePage(page: Int) {
        click(ClickEvent.changePage(page))
    }

    /**
     * Applies a click event that copies [text] to the clipboard.
     */
    public fun copy(text: String) {
        click(ClickEvent.copyToClipboard(text))
    }

    /**
     * Applies a server-side callback click event from [function].
     */
    public fun callback(function: ClickCallback<Audience>) {
        click(ClickEvent.callback(function))
    }

    /**
     * Applies a server-side callback click event from [function] with [uses] and [lifetime].
     */
    public fun callback(
        uses: Int,
        lifetime: KotlinDuration,
        function: ClickCallback<Audience>,
    ) {
        click(
            ClickEvent.callback(function) { options ->
                options.uses(uses)
                options.lifetime(lifetime.toJavaDuration())
            },
        )
    }

    /**
     * Applies a server-side callback click event from [function] with prebuilt [options].
     */
    public fun callback(
        options: ClickCallback.Options,
        function: ClickCallback<Audience>,
    ) {
        click(ClickEvent.callback(function, options))
    }
}
