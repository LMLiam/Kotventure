package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor

private const val RGB_MASK = 0x00FFFFFF
private const val ALPHA_SHIFT = 24
private const val OPAQUE_ALPHA = 0xFF

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
 * Returns the Kotventure colour expression for [color].
 *
 * The sixteen named colours use their DSL properties. Other colours use `hex("#RRGGBB")`.
 */
internal fun colorLiteral(color: TextColor): String =
    when (color) {
        is NamedTextColor -> namedColorLiterals.getValue(color)
        else -> "hex(${quoted(color.asHexString().uppercase())})"
    }

/**
 * Returns the Kotventure shadow-colour arguments for [color].
 *
 * A non-opaque colour includes an `alpha` argument. The caller adds the `shadow` call.
 */
internal fun shadowColorLiteral(color: ShadowColor): String {
    val argb = color.value()
    val rgb = argb and RGB_MASK
    val alpha = argb ushr ALPHA_SHIFT

    val colorLiteral =
        "hex(${quoted("#${rgb.toUpperHex(digits = 6)}")})"

    return if (alpha == OPAQUE_ALPHA) {
        colorLiteral
    } else {
        "$colorLiteral, alpha = 0x${alpha.toUpperHex(digits = 2)}"
    }
}

private fun Int.toUpperHex(digits: Int): String =
    toString(radix = 16)
        .uppercase()
        .padStart(digits, '0')
