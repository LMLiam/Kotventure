package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.event.HoverEventSource

/**
 * Scope for applying Adventure hover events.
 */
@KotventureDslMarker
public interface HoverScope {
    /**
     * Applies [source] as the hover event, or clears the hover event when [source] is null.
     *
     * @throws IllegalStateException when the hover event is already set in this block.
     */
    public fun hover(source: HoverEventSource<*>?)

    /**
     * Applies an Adventure hover event built from a typed [action] and [value].
     *
     * @throws IllegalArgumentException when Adventure rejects the action/value pair.
     */
    public fun <V : Any> hover(
        action: HoverEvent.Action<V>,
        value: V,
    ) {
        hover(HoverEvent.hoverEvent(action, value))
    }

    /**
     * Applies a hover event built from a Kotventure hover-content DSL block.
     *
     * @throws IllegalStateException when [init] does not choose exactly one hover payload.
     * @throws IllegalArgumentException when a payload value is rejected before reaching Adventure.
     */
    public fun hover(init: HoverContentScope.() -> Unit) {
        hover(buildHoverEvent(init))
    }
}
