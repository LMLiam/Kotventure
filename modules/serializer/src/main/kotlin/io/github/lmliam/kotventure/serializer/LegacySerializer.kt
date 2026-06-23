package io.github.lmliam.kotventure.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer

/**
 * Serializes this component to legacy ampersand (`&`) formatting.
 */
public fun Component.toLegacyAmpersand(): String = LegacyComponentSerializer.legacyAmpersand().serialize(this)

/**
 * Deserializes this legacy ampersand (`&`) string to an Adventure component.
 */
public fun String.asLegacyAmpersandComponent(): Component =
    LegacyComponentSerializer.legacyAmpersand().deserialize(this)

/**
 * Serializes this component to legacy section-sign formatting.
 */
public fun Component.toLegacySection(): String = LegacyComponentSerializer.legacySection().serialize(this)

/**
 * Deserializes this legacy section-sign string to an Adventure component.
 */
public fun String.asLegacySectionComponent(): Component = LegacyComponentSerializer.legacySection().deserialize(this)
