package io.github.lmliam.kotventure.serializer

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer
import net.kyori.adventure.text.serializer.json.JSONOptions
import net.kyori.adventure.text.serializer.json.legacyimpl.NBTLegacyHoverEventSerializer
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer

private val jsonSerializer: GsonComponentSerializer =
    GsonComponentSerializer
        .builder()
        .options(JSONOptions.compatibility())
        .legacyHoverEventSerializer(NBTLegacyHoverEventSerializer.get())
        .build()

/**
 * Serializes this component to legacy ampersand (`&`) formatting.
 */
public fun Component.toLegacy(): String = LegacyComponentSerializer.legacyAmpersand().serialize(this)

/**
 * Deserializes this legacy ampersand (`&`) string to an Adventure component.
 */
public fun String.asLegacyComponent(): Component = LegacyComponentSerializer.legacyAmpersand().deserialize(this)

/**
 * Serializes this component to legacy section-sign formatting.
 */
public fun Component.toSection(): String = LegacyComponentSerializer.legacySection().serialize(this)

/**
 * Deserializes this legacy section-sign string to an Adventure component.
 */
public fun String.asSectionComponent(): Component = LegacyComponentSerializer.legacySection().deserialize(this)

/**
 * Serializes this component to Adventure's JSON component format.
 */
public fun Component.toJson(): String = jsonSerializer.serialize(this)

/**
 * Deserializes this Adventure JSON component string to an Adventure component.
 */
public fun String.asJsonComponent(): Component = jsonSerializer.deserialize(this)

/**
 * Serializes this component to MiniMessage markup using Adventure's default MiniMessage serializer.
 */
public fun Component.toMini(): String = MiniMessage.miniMessage().serialize(this)

/**
 * Deserializes this MiniMessage string to an Adventure component.
 */
public fun String.asMiniComponent(): Component = MiniMessage.miniMessage().deserialize(this)

/**
 * Serializes this component to plain text using Adventure's default plain text serializer.
 */
public fun Component.toPlain(): String = PlainTextComponentSerializer.plainText().serialize(this)

/**
 * Deserializes this plain text string to an Adventure text component.
 */
public fun String.asPlainComponent(): Component = PlainTextComponentSerializer.plainText().deserialize(this)
