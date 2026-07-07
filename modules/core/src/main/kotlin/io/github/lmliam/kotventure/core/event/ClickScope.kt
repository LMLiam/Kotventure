package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.event.ClickEvent

/**
 * Scope for applying Adventure click events.
 */
@KotventureDslMarker
public interface ClickScope {
    /**
     * Applies [event] as the click event, or clears the click event when [event] is null.
     *
     * @throws IllegalStateException when the click event is already set in this block.
     */
    public fun click(event: ClickEvent<*>?)

    /**
     * Applies an Adventure click event built from a typed [action] and [payload].
     *
     * @throws IllegalStateException when the click event is already set in this block.
     * @throws IllegalArgumentException when Adventure rejects the action/payload pair.
     */
    public fun <P : ClickEvent.Payload> click(
        action: ClickEvent.Action<P>,
        payload: P,
    ) {
        click(ClickEvent.clickEvent(action, payload))
    }

    /**
     * Applies a click event built from a Kotventure click-action DSL block.
     *
     * @throws IllegalStateException when [init] does not choose exactly one click action, or when the click
     *         event is already set in this block.
     * @throws IllegalArgumentException when Adventure rejects the selected action payload.
     */
    public fun click(init: ClickActionScope.() -> Unit) {
        click(buildClickEvent(init))
    }
}
