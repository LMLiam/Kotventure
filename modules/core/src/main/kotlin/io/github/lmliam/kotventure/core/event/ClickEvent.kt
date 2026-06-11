package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import java.net.URI
import java.nio.file.Path
import java.time.temporal.TemporalAmount
import kotlin.time.toJavaDuration
import kotlin.time.Duration as KotlinDuration

internal fun openTarget(target: String): ClickEvent<ClickEvent.Payload.Text> =
    fileUriPath(target)
        ?.let { file -> ClickEvent.openFile(file) }
        ?: ClickEvent.openUrl(target)

internal fun openUrlTarget(url: String): ClickEvent<ClickEvent.Payload.Text> = ClickEvent.openUrl(url)

/**
 * Builds an Adventure click event that opens this URL or file URI.
 */
@JvmName("openString")
public fun String.open(): ClickEvent<ClickEvent.Payload.Text> = openTarget(this)

internal fun openFileTarget(file: String): ClickEvent<ClickEvent.Payload.Text> = ClickEvent.openFile(file)

internal fun runTarget(command: String): ClickEvent<ClickEvent.Payload.Text> = ClickEvent.runCommand(command)

internal fun suggestTarget(command: String): ClickEvent<ClickEvent.Payload.Text> = ClickEvent.suggestCommand(command)

internal fun changePageTarget(page: Int): ClickEvent<ClickEvent.Payload.Int> = ClickEvent.changePage(page)

internal fun copyTarget(text: String): ClickEvent<ClickEvent.Payload.Text> = ClickEvent.copyToClipboard(text)

/**
 * Builds an Adventure click event that copies this text to the clipboard.
 */
@JvmName("copyString")
public fun String.copy(): ClickEvent<ClickEvent.Payload.Text> = copyTarget(this)

internal fun callbackTarget(function: ClickCallback<Audience>): ClickEvent<*> = ClickEvent.callback(function)

internal fun callbackTarget(
    uses: Int,
    lifetime: TemporalAmount,
    function: ClickCallback<Audience>,
): ClickEvent<*> =
    ClickEvent.callback(function) { options ->
        options.uses(uses)
        options.lifetime(lifetime)
    }

internal fun callbackTarget(
    uses: Int,
    lifetime: KotlinDuration,
    function: ClickCallback<Audience>,
): ClickEvent<*> = callbackTarget(uses, lifetime.toJavaDuration(), function)

internal fun callbackTarget(
    options: ClickCallback.Options,
    function: ClickCallback<Audience>,
): ClickEvent<*> = ClickEvent.callback(function, options)

/**
 * Builds an Adventure click event from a typed [action] and [payload].
 *
 * @throws IllegalArgumentException when Adventure rejects the action/payload pair.
 */
public fun <P : ClickEvent.Payload> clickEvent(
    action: ClickEvent.Action<P>,
    payload: P,
): ClickEvent<P> = ClickEvent.clickEvent(action, payload)

private fun fileUriPath(target: String): String? {
    val uri =
        runCatching {
            URI(target)
        }.getOrNull() ?: return null

    if (!uri.scheme.equals("file", ignoreCase = true)) {
        return null
    }

    return runCatching {
        Path.of(uri).toString()
    }.getOrElse {
        target
    }
}
