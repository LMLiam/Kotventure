package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.`object`.ObjectContents
import net.kyori.adventure.text.`object`.SpriteObjectContents
import java.util.Locale

/** Renders [color] as the Kotventure colour-DSL expression that reconstructs it. */
internal fun colorLiteral(color: TextColor): String =
    if (color is NamedTextColor) {
        "NamedTextColor.${color.name().uppercase(Locale.ROOT)}"
    } else {
        "TextColor.color(0x${color.asHexString().removePrefix("#").uppercase(Locale.ROOT)})"
    }

/** Renders [key] as a `key("namespace", "value")` call. */
internal fun keyLiteral(key: Key): String =
    "key(\"${escapeKotlinString(key.namespace())}\", \"${escapeKotlinString(key.value())}\")"

/**
 * Renders [contents] as the object-contents expression that reconstructs it, using the single-argument `sprite` form
 * when the sprite uses Adventure's default atlas.
 *
 * `<head:...>` produces player-head contents, which the DSL has no surface for; they are rejected so the head is not
 * silently replaced by an empty display.
 */
internal fun objectContentsLiteral(contents: ObjectContents): String =
    when (contents) {
        is SpriteObjectContents ->
            if (contents.atlas() == SpriteObjectContents.DEFAULT_ATLAS) {
                "sprite(${keyLiteral(contents.sprite())})"
            } else {
                "sprite(${keyLiteral(contents.atlas())}, ${keyLiteral(contents.sprite())})"
            }

        else -> throw IllegalArgumentException(
            "miniToDsl cannot represent player-head object contents: the component DSL only has a sprite surface.",
        )
    }

/** Renders [value] as the Kotlin expression that reconstructs it. */
internal fun dataComponentValueLiteral(value: DataComponentValue): String =
    when (value) {
        is BinaryTagHolder -> "BinaryTagHolder.binaryTagHolder(\"${escapeKotlinString(value.string())}\")"
        is DataComponentValue.TagSerializable ->
            "BinaryTagHolder.binaryTagHolder(\"${escapeKotlinString(value.asBinaryTag().string())}\")"

        is DataComponentValue.Removed -> "DataComponentValue.removed()"
        else -> throw IllegalArgumentException(
            "miniToDsl cannot represent data component value ${value::class.qualifiedName}.",
        )
    }

/** Escapes [value] for embedding inside a double-quoted Kotlin string literal. */
internal fun escapeKotlinString(value: String): String =
    buildString {
        value.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\n' -> append("\\n")
                '\r' -> append("\\r")
                '\t' -> append("\\t")
                '$' -> append('\\').append('$')
                else -> append(character)
            }
        }
    }
