package io.github.lmliam.kotventure.core.audience

import net.kyori.adventure.audience.Audience
import net.kyori.adventure.chat.SignedMessage

/**
 * Builds a component and its bound chat type from a [ChatScope] block and sends it to this
 * [Audience] as a non-system, player-styled chat message.
 *
 * Works for any audience — a player, the console, or a forwarding audience over many members.
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
 * Builds a bound chat type from a [BoundChatScope] block and sends the already-signed [signed]
 * message to this [Audience] with it.
 *
 * A genuine player-signed [SignedMessage] cannot be constructed by hand — obtain one from a
 * platform source such as Paper's chat event (`signedMessage()`) or its Brigadier signed-message
 * command argument; server-authored system messages come from [systemMessage].
 *
 * @throws IllegalStateException when the block leaves `name` unset, or sets any slot twice.
 * @sample io.github.lmliam.kotventure.core.audience.signedChatSample
 */
public fun Audience.chat(
    signed: SignedMessage,
    init: BoundChatScope.() -> Unit,
): Unit = sendMessage(signed, BoundChatBuilder().apply(init).buildBound())
