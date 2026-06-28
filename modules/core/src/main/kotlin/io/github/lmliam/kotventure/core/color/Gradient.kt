package io.github.lmliam.kotventure.core.color

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import kotlin.math.floor
import kotlin.math.min

/**
 * Immutable color gradient that interpolates across an ordered list of at least two [TextColor] stops.
 *
 * @throws IllegalArgumentException if fewer than two stops are supplied.
 */
public class ColorGradient public constructor(
    stops: Iterable<TextColor>,
) {
    /** The gradient's color stops, in order, as a defensive immutable copy. */
    public val stops: List<TextColor> =
        stops.toList().also { copiedStops ->
            require(copiedStops.size >= 2) {
                "A color gradient requires at least 2 stops."
            }
        }

    /**
     * Returns the color at [progress] along the gradient.
     *
     * @param progress the position, where `0f` is the first stop and `1f` the last; values outside `0f..1f`
     *   are clamped.
     * @throws IllegalArgumentException if [progress] is not finite.
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
 * Creates a [ColorGradient] from two or more color stops.
 *
 * @sample io.github.lmliam.kotventure.core.color.gradientSample
 *
 * @throws IllegalArgumentException if fewer than two stops are supplied.
 */
public fun gradient(vararg stops: TextColor): ColorGradient = ColorGradient(stops.asList())

/**
 * Creates a [ColorGradient] from an iterable of two or more color stops.
 *
 * @throws IllegalArgumentException if fewer than two stops are supplied.
 */
public fun gradient(stops: Iterable<TextColor>): ColorGradient = ColorGradient(stops)

/**
 * Builds a component that spreads [stops] across [value], coloring one child per code point.
 *
 * @sample io.github.lmliam.kotventure.core.color.gradientTextSample
 *
 * @param value the text to color; its code points (not chars) are colored so surrogate pairs stay intact.
 * @throws IllegalArgumentException if fewer than two stops are supplied.
 */
public fun gradientText(
    value: String,
    vararg stops: TextColor,
): Component = gradientText(value, gradient(*stops))

/**
 * Builds a component that spreads [gradient] across [value], coloring one child per code point.
 *
 * @param value the text to color; its code points (not chars) are colored so surrogate pairs stay intact.
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
