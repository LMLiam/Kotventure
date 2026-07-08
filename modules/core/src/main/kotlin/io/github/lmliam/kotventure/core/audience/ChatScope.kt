package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.text.ComponentLike

/**
 * Scope for configuring the bound chat type of a player-styled chat message: the sender [name], an
 * optional [target], and the [type] of chat the client renders it as.
 */
@KotventureDslMarker
public interface BoundChatScope {
    /**
     * Sets the type of chat the message renders as, such as [ChatType.MSG_COMMAND_INCOMING].
     *
     * When not set, the message renders as ordinary player chat ([ChatType.CHAT]).
     *
     * @throws IllegalStateException when the type is already set in this block.
     */
    public fun type(type: ChatType)

    /**
     * Builds the sender name shown alongside the message from a component DSL block.
     *
     * @throws IllegalStateException when the name is already set in this block.
     */
    public fun name(init: ComponentScope.() -> Unit)

    /**
     * Sets the sender name shown alongside the message, such as a player's display name.
     *
     * @throws IllegalStateException when the name is already set in this block.
     */
    public fun <T : ComponentLike> name(component: T)

    /**
     * Builds the message target shown by directed chat types (such as the `/msg` commands) from a
     * component DSL block.
     *
     * @throws IllegalStateException when the target is already set in this block.
     */
    public fun target(init: ComponentScope.() -> Unit)

    /**
     * Sets the message target shown by directed chat types (such as the `/msg` commands).
     *
     * @throws IllegalStateException when the target is already set in this block.
     */
    public fun <T : ComponentLike> target(component: T)
}

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
