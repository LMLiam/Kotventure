package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.text.event.HoverEvent

/**
 * Creates a reusable hover event from the payload that [init] selects.
 *
 * Select exactly one of [HoverContentScope.text], [HoverContentScope.item], or [HoverContentScope.entity]. This
 * function does not attach the event to a component.
 *
 * @sample io.github.lmliam.kotventure.core.event.hoverSample
 *
 * @throws IllegalStateException when [init] does not choose exactly one hover payload.
 * @throws IllegalArgumentException when a payload value is rejected before reaching Adventure.
 */
public fun hover(init: HoverContentScope.() -> Unit): HoverEvent<*> = buildHoverEvent(init)

internal fun buildHoverEvent(init: HoverContentScope.() -> Unit): HoverEvent<*> =
    HoverBuilder()
        .apply(init)
        .build()

/**
 * Creates an Adventure hover event from a typed [action] and [value].
 *
 * @throws IllegalArgumentException when Adventure rejects the action/value pair.
 */
public fun <V : Any> hoverEvent(
    action: HoverEvent.Action<V>,
    value: V,
): HoverEvent<V> = HoverEvent.hoverEvent(action, value)
