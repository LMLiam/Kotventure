package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.text.event.HoverEvent

/**
 * Builds a reusable Adventure hover event from a Kotventure hover-content DSL block.
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
 * Builds an Adventure hover event from a typed [action] and [value].
 *
 * @throws IllegalArgumentException when Adventure rejects the action/value pair.
 */
public fun <V : Any> hoverEvent(
    action: HoverEvent.Action<V>,
    value: V,
): HoverEvent<V> = HoverEvent.hoverEvent(action, value)
