package io.github.lmliam.kotventure.minimessage.conversion

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

/** Returns [value] as an escaped, double-quoted Kotlin string literal. */
internal fun quoted(value: String): String = "\"${escapeKotlinString(value)}\""

/**
 * Returns the Kotventure colour expression for [color].
 *
 * The sixteen named colours use their DSL properties. Other colours use `hex("#RRGGBB")`.
 */
internal fun colorLiteral(color: TextColor): String =
    when (color) {
        is NamedTextColor -> NAMED_COLOR_LITERALS.getValue(color)
        else -> "hex(${quoted(color.asHexString().uppercase(Locale.ROOT))})"
    }

/**
 * Returns the Kotventure shadow-colour arguments for [color].
 *
 * A non-opaque colour includes an `alpha` argument. The caller adds the `shadow` call.
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

/** Returns [key] as a `key("namespace", "value")` call. */
internal fun keyLiteral(key: Key): String = "key(${quoted(key.namespace())}, ${quoted(key.value())})"

/**
 * Returns an object-contents expression for [contents].
 *
 * A sprite in Adventure's default atlas uses the one-argument `sprite` call. Player-head contents use the applicable
 * `head` call.
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
 * Returns the lossless `head` expression for [contents].
 *
 * The converter requires one skin source and no profile properties. It rejects other states because the DSL cannot
 * represent them without loss.
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

/** Escapes [value] for a double-quoted Kotlin string literal. */
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
                else ->
                    if (character.isISOControl()) {
                        append("\\u%04X".format(Locale.ROOT, character.code))
                    } else {
                        append(character)
                    }
            }
        }
    }

/** Returns a [Byte] literal and parenthesises a negative value. */
internal fun kotlinByteLiteral(value: Byte): String = if (value < 0) "($value).toByte()" else "$value.toByte()"

/** Returns a [Short] literal and parenthesises a negative value. */
internal fun kotlinShortLiteral(value: Short): String = if (value < 0) "($value).toShort()" else "$value.toShort()"

/** Returns an [Int] literal, including the [Int.MIN_VALUE] constant. */
internal fun kotlinIntLiteral(value: Int): String = if (value == Int.MIN_VALUE) "Int.MIN_VALUE" else value.toString()

/** Returns a [Long] literal, including the [Long.MIN_VALUE] constant. */
internal fun kotlinLongLiteral(value: Long): String = if (value == Long.MIN_VALUE) "Long.MIN_VALUE" else "${value}L"

/** Returns a [Float] literal, including non-finite constants. */
internal fun kotlinFloatLiteral(value: Float): String =
    when {
        value.isNaN() -> "Float.NaN"
        value == Float.POSITIVE_INFINITY -> "Float.POSITIVE_INFINITY"
        value == Float.NEGATIVE_INFINITY -> "Float.NEGATIVE_INFINITY"
        else -> "${value}f"
    }

/** Returns a [Double] literal, including non-finite constants. */
internal fun kotlinDoubleLiteral(value: Double): String =
    when {
        value.isNaN() -> "Double.NaN"
        value == Double.POSITIVE_INFINITY -> "Double.POSITIVE_INFINITY"
        value == Double.NEGATIVE_INFINITY -> "Double.NEGATIVE_INFINITY"
        else -> value.toString()
    }

/** Returns [value] as the applicable Kotlin numeric literal. */
internal fun kotlinNumberLiteral(value: Number): String =
    when (value) {
        is Double -> kotlinDoubleLiteral(value)
        is Float -> kotlinFloatLiteral(value)
        is Long -> kotlinLongLiteral(value)
        is Byte -> kotlinByteLiteral(value)
        is Short -> kotlinShortLiteral(value)
        is Int -> kotlinIntLiteral(value)
        else -> value.toString()
    }
