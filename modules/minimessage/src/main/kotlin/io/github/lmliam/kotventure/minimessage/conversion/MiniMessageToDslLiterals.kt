package io.github.lmliam.kotventure.minimessage.conversion

import io.github.lmliam.kotventure.core.selector.EntitySelectorParseException
import io.github.lmliam.kotventure.core.selector.entitySelector
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.`object`.ObjectContents
import net.kyori.adventure.text.`object`.PlayerHeadObjectContents
import net.kyori.adventure.text.`object`.SpriteObjectContents
import java.util.Locale

private val NAMED_COLOR_LITERALS: Map<NamedTextColor, String> =
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

private fun quoted(value: String): String = "\"${escapeKotlinString(value)}\""

/**
 * Renders [color] as the Kotventure colour-DSL expression that reconstructs it: a named-colour property
 * (`gold`, `darkBlue`, ...) for the sixteen named colours, otherwise a `hex("#RRGGBB")` call.
 */
internal fun colorLiteral(color: TextColor): String =
    when (color) {
        is NamedTextColor -> NAMED_COLOR_LITERALS.getValue(color)
        else -> "hex(${quoted(color.asHexString().uppercase(Locale.ROOT))})"
    }

/**
 * Renders [color] as the DSL expression that reconstructs it by composing
 * [hex][io.github.lmliam.kotventure.core.color.hex] and the
 * [shadow][io.github.lmliam.kotventure.core.style.StyleScope.shadow] overload.
 *
 * Emits `hex("#RRGGBB"), alpha = 0xAA` when alpha differs from the default (0xFF),
 * otherwise just `hex("#RRGGBB")`. The call site wraps this in `shadow(...)`.
 */
internal fun shadowColorLiteral(color: ShadowColor): String {
    val argb = color.value()
    val rgb = argb and 0x00FFFFFF
    val alpha = argb ushr 24
    val hexLiteral = "hex(${quoted("#%06X".format(Locale.ROOT, rgb))})"

    return if (alpha == 0xFF) {
        hexLiteral
    } else {
        "$hexLiteral, alpha = 0x%02X".format(Locale.ROOT, alpha)
    }
}

/** Renders [key] as a `key("namespace", "value")` call. */
internal fun keyLiteral(key: Key): String = "key(${quoted(key.namespace())}, ${quoted(key.value())})"

/**
 * Validates raw selector [source] and renders it as the canonical `entitySelector("...")` call.
 *
 * @throws EntitySelectorParseException if [source] is not valid selector syntax
 */
internal fun entitySelectorLiteral(source: String): String =
    "entitySelector(${quoted(entitySelector(source).toString())})"

/**
 * Renders [contents] as the object-contents expression that reconstructs it, using the single-argument `sprite` form
 * when the sprite uses Adventure's default atlas, and the matching `head` form for player-head contents.
 */
internal fun objectContentsLiteral(contents: ObjectContents): String =
    when (contents) {
        is SpriteObjectContents -> {
            val atlas = contents.atlas()
            val sprite = keyLiteral(contents.sprite())

            if (atlas == SpriteObjectContents.DEFAULT_ATLAS) {
                "sprite($sprite)"
            } else {
                "sprite(${keyLiteral(atlas)}, $sprite)"
            }
        }

        is PlayerHeadObjectContents -> playerHeadLiteral(contents)
    }

/**
 * Renders [contents] as the `head(...)` call that reconstructs it. The `<head>` tag sets exactly one skin source -
 * a name, a UUID, or a texture key - plus the hat flag. Profile properties and multiple skin sources are not
 * producible by the tag and have no DSL surface, so they are rejected rather than silently dropped.
 */
private fun playerHeadLiteral(contents: PlayerHeadObjectContents): String {
    if (contents.profileProperties().isNotEmpty()) {
        conversionError("miniToDsl cannot represent player-head profile properties: the <head> tag does not set them.")
    }

    val skinSource =
        listOfNotNull(
            contents.name()?.let(::quoted),
            contents.id()?.let { "uuid(${quoted(it.toString())})" },
            contents.texture()?.let(::keyLiteral),
        ).singleOrNull()
            ?: conversionError(
                "miniToDsl cannot represent a player head without exactly one skin source (a name, UUID, or texture).",
            )

    return buildString {
        append("head(")
        append(skinSource)
        if (!contents.hat()) {
            append(", hat = false")
        }
        append(")")
    }
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
