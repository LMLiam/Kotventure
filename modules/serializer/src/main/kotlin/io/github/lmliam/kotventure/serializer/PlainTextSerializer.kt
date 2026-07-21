package io.github.lmliam.kotventure.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

/**
 * Serialises this component to plain text with Adventure's default serialiser.
 *
 * The conversion discards styles and events. Adventure determines the text representation of each
 * non-text component type.
 *
 * @return the plain-text representation of this component.
 */
public fun Component.toPlainText(): String = PlainTextComponentSerializer.plainText().serialize(this)
