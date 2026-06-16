package io.github.lmliam.kotventure.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

/**
 * Serializes this component to legacy ampersand (`&`) formatting.
 */
public fun Component.toLegacy(): String = LegacyComponentSerializer.legacyAmpersand().serialize(this)

/**
 * Deserializes this legacy ampersand (`&`) string to an Adventure component.
 */
public fun String.legacy(): Component = LegacyComponentSerializer.legacyAmpersand().deserialize(this)

/**
 * Serializes this component to legacy section-sign formatting.
 */
public fun Component.toSection(): String = LegacyComponentSerializer.legacySection().serialize(this)

/**
 * Deserializes this legacy section-sign string to an Adventure component.
 */
public fun String.section(): Component = LegacyComponentSerializer.legacySection().deserialize(this)

/**
 * Serializes this component to Adventure's JSON component format.
 */
public fun Component.toJson(): String = GsonComponentSerializer.gson().serialize(this)

/**
 * Deserializes this Adventure JSON component string to an Adventure component.
 */
public fun String.fromJson(): Component = GsonComponentSerializer.gson().deserialize(this)

/**
 * Serializes this component to MiniMessage markup using Adventure's default MiniMessage serializer.
 */
public fun Component.toMini(): String = MiniMessage.miniMessage().serialize(this)

/**
 * Deserializes this MiniMessage string to an Adventure component.
 */
public fun String.mini(): Component = MiniMessage.miniMessage().deserialize(this)

/**
 * Serializes this component to MiniMessage markup using Adventure's default MiniMessage serializer.
 */
public fun Component.toMiniMessage(): String = toMini()

/**
 * Serializes this component to plain text using Adventure's default plain text serializer.
 */
public fun Component.toPlain(): String = PlainTextComponentSerializer.plainText().serialize(this)

/**
 * Deserializes this plain text string to an Adventure text component.
 */
public fun String.plain(): Component = PlainTextComponentSerializer.plainText().deserialize(this)

/**
 * Serializes this component to plain text using Adventure's default plain text serializer.
 */
public fun Component.toPlainText(): String = toPlain()
