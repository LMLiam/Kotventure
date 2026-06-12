package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.text.event.ClickEvent
import java.net.URI
import java.nio.file.Path

internal fun openTarget(target: String): ClickEvent<ClickEvent.Payload.Text> =
    fileUriPath(target)
        ?.let { file -> ClickEvent.openFile(file) }
        ?: ClickEvent.openUrl(target)

/**
 * Builds a reusable Adventure click event from a Kotventure click-action DSL block.
 *
 * @throws IllegalStateException when [init] does not choose exactly one click action.
 * @throws IllegalArgumentException when Adventure rejects the selected action payload.
 */
public fun click(init: ClickActionScope.() -> Unit): ClickEvent<*> = buildClickEvent(init)

internal fun buildClickEvent(init: ClickActionScope.() -> Unit): ClickEvent<*> =
    ClickBuilder()
        .apply(init)
        .build()

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

    if (uri.scheme?.equals("file", ignoreCase = true) != true) {
        return null
    }

    return runCatching {
        Path.of(uri).toString()
    }.getOrNull()
}
