package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.text.event.ClickEvent

/**
 * Creates a reusable click event from the action that [init] selects.
 *
 * Select exactly one action. The available actions include [ClickActionScope.openUrl], [ClickActionScope.openFile],
 * [ClickActionScope.run], [ClickActionScope.suggest], [ClickActionScope.changePage], [ClickActionScope.copy], and
 * [ClickActionScope.callback]. This function does not attach the event to a component.
 *
 * @sample io.github.lmliam.kotventure.core.event.clickSample
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
 * Creates an Adventure click event from a typed [action] and [payload].
 *
 * @throws IllegalArgumentException when Adventure rejects the action/payload pair.
 */
public fun <P : ClickEvent.Payload> clickEvent(
    action: ClickEvent.Action<P>,
    payload: P,
): ClickEvent<P> = ClickEvent.clickEvent(action, payload)
