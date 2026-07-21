package io.github.lmliam.kotventure.core.audience

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.chat.SignedMessage

/**
 * Creates a component and bound chat type from [init], then sends a player-styled message to this [Audience].
 *
 * Unlike [message], this function does not send an unattributed system line. The client uses the player-chat pipeline.
 * It attributes the message to the scope's `name`, uses the selected chat-type format, and applies the viewer's chat
 * visibility settings.
 *
 * Works for any audience, such as a player, the console, or a forwarding audience over many members.
 *
 * @throws IllegalStateException when the block leaves `name` or `content` unset, or sets any slot
 *   twice.
 * @sample io.github.lmliam.kotventure.core.audience.chatSample
 */
public fun Audience.chat(init: ChatScope.() -> Unit) {
    val builder = ChatBuilder().apply(init)
    sendMessage(builder.buildContent(), builder.buildBound())
}

/**
 * Creates a bound chat type from [init] and sends the existing [signed] message to this [Audience].
 *
 * You cannot construct a genuine player-signed [SignedMessage] manually. Get one from a platform source, such as
 * Paper's chat event (`signedMessage()`) or its Brigadier signed-message command argument. Use [systemMessage] for a
 * server-authored system message.
 *
 * @throws IllegalStateException when the block leaves `name` unset, or sets any slot twice.
 * @sample io.github.lmliam.kotventure.core.audience.signedChatSample
 */
public fun Audience.chat(
    signed: SignedMessage,
    init: BoundChatScope.() -> Unit,
): Unit = sendMessage(signed, BoundChatBuilder().apply(init).buildBound())
