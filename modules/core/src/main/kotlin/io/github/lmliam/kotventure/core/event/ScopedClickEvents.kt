package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import java.time.temporal.TemporalAmount
import kotlin.time.Duration as KotlinDuration

/**
 * Applies [event] as the click event, or clears the click event when [event] is null.
 */
context(scope: ClickScope)
public fun clickEvent(event: ClickEvent<*>?) {
    scope.click(event)
}

/**
 * Applies a click event that opens [target].
 *
 * File URI targets are converted to open-file events; all other targets are treated as URLs.
 */
context(scope: ClickScope)
public fun open(target: String) {
    scope.click(openTarget(target))
}

/**
 * Applies a click event that opens [url].
 */
context(scope: ClickScope)
public fun openUrl(url: String) {
    scope.click(openUrlTarget(url))
}

/**
 * Applies a click event that opens a local [file] path.
 */
context(scope: ClickScope)
public fun openFile(file: String) {
    scope.click(openFileTarget(file))
}

/**
 * Applies a click event that runs [command].
 */
context(scope: ClickScope)
public fun run(command: String) {
    scope.click(runTarget(command))
}

/**
 * Applies a click event that runs [command].
 */
context(scope: ClickScope)
public fun runCommand(command: String) {
    scope.click(runTarget(command))
}

/**
 * Applies a click event that suggests [command] in chat.
 */
context(scope: ClickScope)
public fun suggest(command: String) {
    scope.click(suggestTarget(command))
}

/**
 * Applies a click event that suggests [command] in chat.
 */
context(scope: ClickScope)
public fun suggestCommand(command: String) {
    scope.click(suggestTarget(command))
}

/**
 * Applies a click event that changes a book to [page].
 */
context(scope: ClickScope)
public fun changePage(page: Int) {
    scope.click(changePageTarget(page))
}

/**
 * Applies a click event that copies [text] to the clipboard.
 */
context(scope: ClickScope)
public fun copy(text: String) {
    scope.click(copyTarget(text))
}

/**
 * Applies a click event that copies [text] to the clipboard.
 */
context(scope: ClickScope)
public fun copyToClipboard(text: String) {
    scope.click(copyTarget(text))
}

/**
 * Applies a server-side callback click event from [function].
 */
context(scope: ClickScope)
public fun callback(function: ClickCallback<Audience>) {
    scope.click(callbackTarget(function))
}

/**
 * Applies a server-side callback click event from [function] with [uses] and [lifetime].
 */
context(scope: ClickScope)
public fun callback(
    uses: Int,
    lifetime: TemporalAmount,
    function: ClickCallback<Audience>,
) {
    scope.click(callbackTarget(uses, lifetime, function))
}

/**
 * Applies a server-side callback click event from [function] with [uses] and [lifetime].
 */
context(scope: ClickScope)
public fun callback(
    uses: Int,
    lifetime: KotlinDuration,
    function: ClickCallback<Audience>,
) {
    scope.click(callbackTarget(uses, lifetime, function))
}

/**
 * Applies a server-side callback click event from [function] with prebuilt [options].
 */
context(scope: ClickScope)
public fun callback(
    options: ClickCallback.Options,
    function: ClickCallback<Audience>,
) {
    scope.click(callbackTarget(options, function))
}
