package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration as KotlinDuration

/**
 * Scope for selecting the single action produced by a click event.
 */
@KotventureDslMarker
public interface ClickActionScope {
    /**
     * Selects a click action that opens [target].
     *
     * File URI targets are converted to open-file events; all other targets are treated as URLs.
     */
    public fun open(target: String)

    /**
     * Selects a click action that opens [url].
     */
    public fun openUrl(url: String)

    /**
     * Selects a click action that opens a local [file] path.
     */
    public fun openFile(file: String)

    /**
     * Selects a click action that runs [command].
     */
    public fun run(command: String)

    /**
     * Selects a click action that suggests [command] in chat.
     */
    public fun suggest(command: String)

    /**
     * Selects a click action that changes a book to [page].
     */
    public fun changePage(page: Int)

    /**
     * Selects a click action that copies [text] to the clipboard.
     */
    public fun copy(text: String)

    /**
     * Selects a server-side callback click action from [function].
     */
    public fun callback(function: ClickCallback<Audience>)

    /**
     * Selects a server-side callback click action from [function] with [uses] and [lifetime].
     */
    public fun callback(
        uses: Int,
        lifetime: KotlinDuration,
        function: ClickCallback<Audience>,
    )

    /**
     * Selects a server-side callback click action from [function] with prebuilt [options].
     */
    public fun callback(
        options: ClickCallback.Options,
        function: ClickCallback<Audience>,
    )
}
