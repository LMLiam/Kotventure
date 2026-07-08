package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Scope for sending a component as a player-styled chat message: the [BoundChatScope] slots plus
 * the message [content] itself.
 */
@KotventureDslMarker
public interface ChatScope : BoundChatScope {
    /**
     * Builds the chat message content from a component DSL block.
     *
     * @throws IllegalStateException when the content is already set in this block.
     */
    public fun content(init: ComponentScope.() -> Unit)

    /**
     * Sets the chat message content.
     *
     * @throws IllegalStateException when the content is already set in this block.
     */
    public fun <T : ComponentLike> content(component: T)
}
