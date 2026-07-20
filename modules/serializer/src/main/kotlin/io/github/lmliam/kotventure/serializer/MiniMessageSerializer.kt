package io.github.lmliam.kotventure.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Serialises this component to MiniMessage markup with Adventure's default serialiser.
 *
 * @sample io.github.lmliam.kotventure.serializer.toMiniMessageSample
 *
 * This function is the inverse of `mini(...)`. A round trip is not always identical because different components can
 * serialise to equivalent markup.
 */
public fun Component.toMiniMessage(): String = MiniMessage.miniMessage().serialize(this)
