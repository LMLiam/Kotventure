package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback

/**
 * Selects the action for a click event.
 *
 * Select exactly one action in each `click { }` block. The block throws [IllegalStateException] if it selects no
 * action or more than one action. Adventure can reject an invalid payload when the scope creates the event.
 *
 * @sample io.github.lmliam.kotventure.core.event.clickActionScopeSample
 */
@KotventureDslMarker
public interface ClickActionScope {
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
     *
     * @throws IllegalArgumentException when Adventure rejects [page].
     */
    public fun changePage(page: Int)

    /**
     * Selects a click action that copies [text] to the clipboard.
     */
    public fun copy(text: String)

    /**
     * Selects a server-side callback click action from [function].
     *
     * Set callback limits in [options]. Unset slots keep the Adventure defaults: one use, and a lifetime of
     * twelve hours.
     *
     * @throws IllegalStateException when [options] sets a slot more than once.
     * @throws IllegalArgumentException when [options] supplies an invalid use count or lifetime.
     */
    public fun callback(
        options: ClickOptionsScope.() -> Unit = {},
        function: ClickCallback<Audience>,
    )
}
