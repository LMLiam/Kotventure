package io.github.lmliam.kotventure.core.color

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.util.HSVLike

private val hexColorPattern: Regex = Regex("#[0-9A-Fa-f]{6}")

/**
 * Creates an Adventure [TextColor] from an exact `#RRGGBB` hex string.
 */
public fun hex(value: String): TextColor {
    require(hexColorPattern.matches(value)) {
        "Hex colors must use the exact #RRGGBB format, but was <$value>."
    }
    return requireNotNull(TextColor.fromHexString(value)) {
        "Adventure could not create a TextColor from <$value>."
    }
}

/**
 * Creates an Adventure [TextColor] from red, green, and blue channels in the `0..255` range.
 */
public fun rgb(
    red: Int,
    green: Int,
    blue: Int,
): TextColor {
    requireColorChannel("red", red)
    requireColorChannel("green", green)
    requireColorChannel("blue", blue)
    return TextColor.color(red, green, blue)
}

/**
 * Creates an Adventure [TextColor] from normalized HSV components in the `0f..1f` range.
 */
public fun hsv(
    hue: Float,
    saturation: Float,
    value: Float,
): TextColor {
    requireHsvComponent("hue", hue)
    requireHsvComponent("saturation", saturation)
    requireHsvComponent("value", value)
    return TextColor.color(HSVLike.hsvLike(hue, saturation, value))
}

/**
 * Linearly interpolates from [start] to [end] using Adventure's [TextColor.lerp] semantics.
 */
public fun interpolate(
    progress: Float,
    start: TextColor,
    end: TextColor,
): TextColor = TextColor.lerp(progress, start, end)

private fun requireColorChannel(
    name: String,
    value: Int,
) {
    require(value in 0..255) {
        "$name must be in the 0..255 range, but was <$value>."
    }
}

private fun requireHsvComponent(
    name: String,
    value: Float,
) {
    require(value in 0f..1f) {
        "$name must be in the 0f..1f range, but was <$value>."
    }
}
