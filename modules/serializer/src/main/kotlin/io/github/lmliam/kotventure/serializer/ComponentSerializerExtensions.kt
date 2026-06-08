package io.github.lmliam.kotventure.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

/**
 * Serializes this component to MiniMessage markup using Adventure's default MiniMessage serializer.
 */
public fun Component.toMiniMessage(): String = MiniMessage.miniMessage().serialize(this)

/**
 * Serializes this component to plain text using Adventure's default plain text serializer.
 */
public fun Component.toPlainText(): String = PlainTextComponentSerializer.plainText().serialize(this)
