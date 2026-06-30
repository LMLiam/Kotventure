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

/**
 * Renders [color] as the Kotventure colour-DSL expression that reconstructs it: a named-colour property
 * (`gold`, `darkBlue`, …) for the sixteen named colours, otherwise a `hex("#RRGGBB")` call.
 */
internal fun colorLiteral(color: TextColor): String =
    if (color is NamedTextColor) {
        namedColorLiterals.getValue(color)
    } else {
        "hex(\"${color.asHexString().uppercase(Locale.ROOT)}\")"
    }

private val namedColorLiterals: Map<NamedTextColor, String> =
    mapOf(
        NamedTextColor.BLACK to "black",
        NamedTextColor.DARK_BLUE to "darkBlue",
        NamedTextColor.DARK_GREEN to "darkGreen",
        NamedTextColor.DARK_AQUA to "darkAqua",
        NamedTextColor.DARK_RED to "darkRed",
        NamedTextColor.DARK_PURPLE to "darkPurple",
        NamedTextColor.GOLD to "gold",
        NamedTextColor.GRAY to "gray",
        NamedTextColor.DARK_GRAY to "darkGray",
        NamedTextColor.BLUE to "blue",
        NamedTextColor.GREEN to "green",
        NamedTextColor.AQUA to "aqua",
        NamedTextColor.RED to "red",
        NamedTextColor.LIGHT_PURPLE to "lightPurple",
        NamedTextColor.YELLOW to "yellow",
        NamedTextColor.WHITE to "white",
    )

/**
 * Renders [color] as the DSL expression that reconstructs it by composing
 * [hex][io.github.lmliam.kotventure.core.color.hex] and the
 * [shadow][io.github.lmliam.kotventure.core.style.StyleScope.shadow] overload.
 *
 * Emits `hex("#RRGGBB"), alpha = 0xAA` when alpha differs from the default (0xFF),
 * otherwise just `hex("#RRGGBB")`. The call site wraps this in `shadow(...)`.
 */
internal fun shadowColorLiteral(color: ShadowColor): String {
    val rgb = color.value() and 0x00FFFFFF
    val alpha = (color.value() ushr 24) and 0xFF
    val hexColor = "#%06X".format(rgb)
    return if (alpha == 0xFF) {
        "hex(\"$hexColor\")"
    } else {
        "hex(\"$hexColor\"), alpha = 0x%02X".format(alpha)
    }
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
    if (contents.profileProperties().isNotEmpty()) {
        conversionError("miniToDsl cannot represent player-head profile properties: the <head> tag does not set them.")
    }
    val skinSources =
        listOfNotNull(
            contents.name()?.let { "\"${escapeKotlinString(it)}\"" },
            contents.id()?.let { "uuid(\"$it\")" },
            contents.texture()?.let { keyLiteral(it) },
        )
    if (skinSources.size != 1) {
        conversionError(
            "miniToDsl cannot represent a player head without exactly one skin source (a name, UUID, or texture).",
        )
    }
    val hat = if (contents.hat()) null else "hat = ${contents.hat()}"
    return "head(${(skinSources + listOfNotNull(hat)).joinToString(", ")})"
}

/** Renders [value] as the Kotlin DSL expression that reconstructs it. */
internal fun dataComponentValueLiteral(value: DataComponentValue): String =
    when (value) {
        is BinaryTagHolder ->
            snbtToDslExpression(value.string())
                ?: "nbt(\"${escapeKotlinString(value.string())}\")"

        is DataComponentValue.TagSerializable -> {
            val snbt = value.asBinaryTag().string()
            snbtToDslExpression(snbt) ?: "nbt(\"${escapeKotlinString(snbt)}\")"
        }

        is DataComponentValue.Removed -> "removed()"
        else -> conversionError("miniToDsl cannot represent data component value ${value::class.qualifiedName}.")
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
