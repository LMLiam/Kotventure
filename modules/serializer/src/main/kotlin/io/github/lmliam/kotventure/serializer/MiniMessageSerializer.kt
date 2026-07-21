package io.github.lmliam.kotventure.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

/**
 * Serialises this component to MiniMessage markup with Adventure's default configuration.
 *
 * Adventure can produce equivalent markup that has a different structure from the source component.
 * Therefore, do not use this output as a stable identity for the component tree.
 *
 * @return MiniMessage markup for this component.
 * @sample io.github.lmliam.kotventure.serializer.toMiniMessageSample
 */
public fun Component.toMiniMessage(): String = MiniMessage.miniMessage().serialize(this)
