package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.`object`.ObjectContents
import net.kyori.adventure.text.`object`.PlayerHeadObjectContents
import net.kyori.adventure.text.`object`.SpriteObjectContents
import java.util.Locale

/** Renders [color] as the Kotventure colour-DSL expression that reconstructs it. */
internal fun colorLiteral(color: TextColor): String =
    if (color is NamedTextColor) {
        "NamedTextColor.${color.name().uppercase(Locale.ROOT)}"
    } else {
        "TextColor.color(0x${color.asHexString().removePrefix("#").uppercase(Locale.ROOT)})"
    }

/** Renders [color] as the `ShadowColor.shadowColor(...)` call that reconstructs its packed ARGB value. */
internal fun shadowColorLiteral(color: ShadowColor): String {
    val argb =
        color
            .value()
            .toUInt()
            .toString(16)
            .uppercase(Locale.ROOT)
            .padStart(8, '0')
    return "ShadowColor.shadowColor(0x$argb.toInt())"
}

/** Renders [key] as a `key("namespace", "value")` call. */
internal fun keyLiteral(key: Key): String =
    "key(\"${escapeKotlinString(key.namespace())}\", \"${escapeKotlinString(key.value())}\")"

/**
 * Renders [contents] as the object-contents expression that reconstructs it, using the single-argument `sprite` form
 * when the sprite uses Adventure's default atlas, and the matching `head` form for player-head contents.
 */
internal fun objectContentsLiteral(contents: ObjectContents): String =
    when (contents) {
        is SpriteObjectContents ->
            if (contents.atlas() == SpriteObjectContents.DEFAULT_ATLAS) {
                "sprite(${keyLiteral(contents.sprite())})"
            } else {
                "sprite(${keyLiteral(contents.atlas())}, ${keyLiteral(contents.sprite())})"
            }

        is PlayerHeadObjectContents -> playerHeadLiteral(contents)
    }

/**
 * Renders [contents] as the `head(...)` call that reconstructs it. The `<head>` tag sets exactly one skin source — a
 * name, a UUID, or a texture key — plus the hat flag. Profile properties and multiple skin sources are not producible
 * by the tag and have no DSL surface, so they are rejected rather than silently dropped.
 */
private fun playerHeadLiteral(contents: PlayerHeadObjectContents): String {
    require(contents.profileProperties().isEmpty()) {
        "miniToDsl cannot represent player-head profile properties: the <head> tag does not set them."
    }
    val skinSources =
        listOfNotNull(
            contents.name()?.let { "\"${escapeKotlinString(it)}\"" },
            contents.id()?.let { "UUID.fromString(\"$it\")" },
            contents.texture()?.let { keyLiteral(it) },
        )
    require(skinSources.size == 1) {
        "miniToDsl cannot represent a player head without exactly one skin source (a name, UUID, or texture)."
    }
    val hat = if (contents.hat() == PlayerHeadObjectContents.DEFAULT_HAT) null else "hat = ${contents.hat()}"
    return "head(${(skinSources + listOfNotNull(hat)).joinToString(", ")})"
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
