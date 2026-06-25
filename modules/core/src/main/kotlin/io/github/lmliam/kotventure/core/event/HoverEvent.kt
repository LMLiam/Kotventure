package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.text.event.HoverEvent

/**
 * Builds a reusable hover event. Choose exactly one payload inside [init] — [HoverContentScope.text], [HoverContentScope.item], or [HoverContentScope.entity].
 *
 * ```kotlin
 * val tooltip = hover { text("Click to teleport") }
 * ```
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
