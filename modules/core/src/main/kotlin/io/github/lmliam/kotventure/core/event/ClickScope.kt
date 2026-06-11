package io.github.lmliam.kotventure.core.event

import net.kyori.adventure.text.event.ClickEvent

/**
 * Scope for applying Adventure click events.
 */
public interface ClickScope {
    /**
     * Applies [event] as the click event, or clears the click event when [event] is null.
     */
    public fun click(event: ClickEvent<*>?)
}
