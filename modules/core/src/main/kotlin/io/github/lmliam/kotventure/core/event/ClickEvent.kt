package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.text.event.ClickEvent
import java.net.URI
import java.nio.file.Path

internal fun openTarget(target: String): ClickEvent<ClickEvent.Payload.Text> =
    fileUriPath(target)
        ?.let { file -> ClickEvent.openFile(file) }
        ?: ClickEvent.openUrl(target)

/**
 * Builds an Adventure click event that opens this URL or file URI.
 */
public fun String.asOpenClickEvent(): ClickEvent<ClickEvent.Payload.Text> = openTarget(this)

/**
 * Builds an Adventure click event that copies this text to the clipboard.
 */
public fun String.asCopyClickEvent(): ClickEvent<ClickEvent.Payload.Text> = ClickEvent.copyToClipboard(this)

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
    }.getOrNull()
}
