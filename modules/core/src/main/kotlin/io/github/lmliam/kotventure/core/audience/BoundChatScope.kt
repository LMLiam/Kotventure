package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.chat.ChatType
import net.kyori.adventure.text.ComponentLike

/**
 * Configures the client presentation of a player-styled chat message.
 *
 * [name] is required. [type] defaults to [chat]. [target] is optional and is useful for directed chat types. Each slot
 * is write-once.
 */
@KotventureDslMarker
public interface BoundChatScope {
    /** Ordinary player chat. This is the [type] that the scope uses when no type is set. */
    public val chat: ChatType get() = ChatType.CHAT

    /** A message sent with the `/say` command. */
    public val sayCommand: ChatType get() = ChatType.SAY_COMMAND

    /** A message received through the `/msg` command. */
    public val msgCommandIncoming: ChatType get() = ChatType.MSG_COMMAND_INCOMING

    /** A message sent with the `/msg` command. */
    public val msgCommandOutgoing: ChatType get() = ChatType.MSG_COMMAND_OUTGOING

    /** A message received through the `/teammsg` command. */
    public val teamMsgCommandIncoming: ChatType get() = ChatType.TEAM_MSG_COMMAND_INCOMING

    /** A message sent with the `/teammsg` command. */
    public val teamMsgCommandOutgoing: ChatType get() = ChatType.TEAM_MSG_COMMAND_OUTGOING

    /** A message sent with the `/me` command. */
    public val emoteCommand: ChatType get() = ChatType.EMOTE_COMMAND

    /**
     * Sets the type of chat the message renders as, such as [msgCommandIncoming].
     *
     * When not set, the message renders as ordinary player chat ([chat]).
     *
     * @throws IllegalStateException when the type is already set in this block.
     */
    public fun type(type: ChatType)

    /**
     * Creates and sets the sender name from a component DSL block.
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
     * Creates and sets the target shown by a directed chat type, such as a `/msg` type.
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
