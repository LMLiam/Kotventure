package io.github.lmliam.kotventure.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Serializes this component to MiniMessage markup using Adventure's default serializer.
 *
 * @sample io.github.lmliam.kotventure.serializer.toMiniMessageSample
 *
 * The inverse of `mini(...)`. Note the round trip is not always identical, as distinct components can
 * serialize to equivalent markup.
 */
public fun Component.toMiniMessage(): String = MiniMessage.miniMessage().serialize(this)
