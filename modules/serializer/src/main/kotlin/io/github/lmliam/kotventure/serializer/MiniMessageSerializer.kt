package io.github.lmliam.kotventure.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Serializes this component to MiniMessage markup using Adventure's default serializer.
 */
public fun Component.toMiniMessage(): String = MiniMessage.miniMessage().serialize(this)
