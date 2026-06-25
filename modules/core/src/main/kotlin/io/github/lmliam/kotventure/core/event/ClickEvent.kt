package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.text.event.ClickEvent

/**
 * Builds a reusable click event. Choose exactly one action inside [init] — [ClickActionScope.openUrl], [ClickActionScope.openFile], [ClickActionScope.run],
 * [ClickActionScope.suggest], [ClickActionScope.changePage], [ClickActionScope.copy], or [ClickActionScope.callback].
 *
 * ```kotlin
 * val link = click { openUrl("https://example.com") }
 * ```
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
