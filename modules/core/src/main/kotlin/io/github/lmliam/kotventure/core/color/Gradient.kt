package io.github.lmliam.kotventure.core.color

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import kotlin.math.floor
import kotlin.math.min

/**
 * Immutable Adventure color gradient made from at least two [TextColor] stops.
 */
public class ColorGradient public constructor(
    stops: Iterable<TextColor>,
) {
    /**
     * Ordered color stops used for interpolation.
     */
    public val stops: List<TextColor> =
        stops.toList().also { copiedStops ->
            require(copiedStops.size >= 2) {
                "A color gradient requires at least 2 stops."
            }
        }

    /**
     * Returns the interpolated color at [progress], where `0f` is the first stop and `1f` is the final stop.
     */
    public fun colorAt(progress: Float): TextColor {
        require(progress.isFinite()) {
            "Gradient progress must be finite, but was <$progress>."
        }
        val clampedProgress = progress.coerceIn(0f, 1f)
        val scaledProgress = clampedProgress * (stops.size - 1)
        val segmentIndex = min(floor(scaledProgress).toInt(), stops.lastIndex - 1)
        val segmentProgress = scaledProgress - segmentIndex
        return interpolate(segmentProgress, stops[segmentIndex], stops[segmentIndex + 1])
    }
}

/**
 * Creates an immutable [ColorGradient] from [stops].
 */
public fun gradient(vararg stops: TextColor): ColorGradient = ColorGradient(stops.asList())

/**
 * Creates an immutable [ColorGradient] from [stops].
 */
public fun gradient(stops: Iterable<TextColor>): ColorGradient = ColorGradient(stops)

/**
 * Builds an Adventure component whose direct children are one colored text component per code point in [value].
 */
public fun gradientText(
    value: String,
    vararg stops: TextColor,
): Component = gradientText(value, gradient(*stops))

/**
 * Builds an Adventure component whose direct children are one colored text component per code point in [value].
 */
public fun gradientText(
    value: String,
    gradient: ColorGradient,
): Component {
    val children = gradientTextChildren(value, gradient)
    if (children.isEmpty()) {
        return Component.empty()
    }

    val builder = Component.text()
    children.forEach { child -> builder.append(child) }
    return builder.build()
}

private fun gradientTextChildren(
    value: String,
    gradient: ColorGradient,
): List<Component> {
    val codePoints = value.toCodePointStrings()
    return codePoints.mapIndexed { index, text ->
        val progress = if (codePoints.size == 1) 0f else index.toFloat() / codePoints.lastIndex.toFloat()
        Component.text(text).color(gradient.colorAt(progress))
    }
}

private fun String.toCodePointStrings(): List<String> =
    codePoints()
        .toArray()
        .map { codePoint -> String(Character.toChars(codePoint)) }
