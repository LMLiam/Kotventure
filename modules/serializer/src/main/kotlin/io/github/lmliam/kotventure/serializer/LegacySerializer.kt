package io.github.lmliam.kotventure.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

/**
 * Serialises this component to legacy text that uses `&` as the control character.
 *
 * Legacy text cannot represent all component data. The conversion can discard information such as
 * click events, hover events, fonts, and insertion text.
 *
 * @return the legacy text representation.
 */
public fun Component.toLegacyAmpersand(): String = LegacyComponentSerializer.legacyAmpersand().serialize(this)

/**
 * Deserialises receiver text that uses `&` legacy control codes.
 *
 * @return a component with the text and supported legacy formatting.
 */
public fun String.asLegacyAmpersandComponent(): Component =
    LegacyComponentSerializer.legacyAmpersand().deserialize(this)

/**
 * Serialises this component to legacy text that uses the section sign (`§`) as the control
 * character.
 *
 * Legacy text cannot represent all component data. The conversion can discard information such as
 * click events, hover events, fonts, and insertion text.
 *
 * @return the legacy text representation.
 */
public fun Component.toLegacySection(): String = LegacyComponentSerializer.legacySection().serialize(this)

/**
 * Deserialises receiver text that uses section-sign legacy control codes.
 *
 * @return a component with the text and supported legacy formatting.
 */
public fun String.asLegacySectionComponent(): Component = LegacyComponentSerializer.legacySection().deserialize(this)
