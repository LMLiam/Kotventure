package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures the content and client presentation of a player-styled chat message.
 *
 * The scope requires one [content] value and one sender name from [BoundChatScope]. Each slot is write-once.
 */
@KotventureDslMarker
public interface ChatScope : BoundChatScope {
    /**
     * Creates and sets the chat message content from a component DSL block.
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
