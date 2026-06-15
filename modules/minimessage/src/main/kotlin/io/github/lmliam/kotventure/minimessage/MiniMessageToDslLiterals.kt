package io.github.lmliam.kotventure.minimessage

import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
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

/** Renders [value] as the Kotlin expression that reconstructs it, rejecting payloads the DSL cannot represent. */
internal fun dataComponentValueLiteral(value: DataComponentValue): String =
    when (value) {
        is BinaryTagHolder -> "BinaryTagHolder.binaryTagHolder(\"${escapeKotlinString(value.string())}\")"
        is DataComponentValue.TagSerializable ->
            "BinaryTagHolder.binaryTagHolder(\"${escapeKotlinString(value.asBinaryTag().string())}\")"

        is DataComponentValue.Removed -> "DataComponentValue.removed()"
        else -> throw IllegalArgumentException(
            "miniToDsl does not yet support data component value ${value::class.qualifiedName}.",
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
