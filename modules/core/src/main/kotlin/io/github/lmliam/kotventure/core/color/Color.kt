package io.github.lmliam.kotventure.core.color

import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.util.HSVLike
import org.jetbrains.annotations.Range

private val hexColorPattern: Regex = Regex("#[0-9A-Fa-f]{6}")

/**
 * Creates a [TextColor] from an exact `#RRGGBB` hex string.
 *
 * @sample io.github.lmliam.kotventure.core.color.hexColorSample
 *
 * @param value a six-digit hex string with a leading `#`, e.g. `"#FF0000"`.
 * @throws IllegalArgumentException if [value] is not exactly `#RRGGBB`.
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
 * Creates a [TextColor] from red, green, and blue channels.
 *
 * @param red the red channel, `0..255`.
 * @param green the green channel, `0..255`.
 * @param blue the blue channel, `0..255`.
 * @throws IllegalArgumentException if any channel is outside `0..255`.
 */
public fun rgb(
    red: @Range(from = 0, to = 255) Int,
    green: @Range(from = 0, to = 255) Int,
    blue: @Range(from = 0, to = 255) Int,
): TextColor {
    requireColorChannel("red", red)
    requireColorChannel("green", green)
    requireColorChannel("blue", blue)
    return TextColor.color(red, green, blue)
}

/**
 * Creates a [TextColor] from normalized HSV components.
 *
 * @param hue the hue, `0f..1f`.
 * @param saturation the saturation, `0f..1f`.
 * @param value the brightness value, `0f..1f`.
 * @throws IllegalArgumentException if any component is outside `0f..1f`.
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
 * Uses Adventure's [TextColor.lerp] rules to interpolate linearly between two colours.
 *
 * @param progress the position between the colours. `0f` returns [start], and `1f` returns [end]. Values outside
 *   `0f..1f` use Adventure's extrapolation rules.
 * @param start the colour at `progress` `0f`.
 * @param end the colour at `progress` `1f`.
 * @throws IllegalArgumentException if [progress] is not finite.
 */
public fun interpolate(
    progress: Float,
    start: TextColor,
    end: TextColor,
): TextColor {
    require(progress.isFinite()) {
        "progress must be finite, but was <$progress>."
    }
    return TextColor.lerp(progress, start, end)
}

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
