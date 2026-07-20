package io.github.lmliam.kotventure.core.color

import io.github.lmliam.kotventure.core.component.emptyComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import kotlin.math.floor
import kotlin.math.min

/**
 * An immutable colour gradient with an ordered list of two or more [TextColor] stops.
 *
 * Use [gradient] to create an instance. The internal constructor gives construction one public entry point.
 *
 * @throws IllegalArgumentException if fewer than two stops are supplied.
 */
public class ColorGradient internal constructor(
    stops: Iterable<TextColor>,
) {
    /** An immutable copy of the gradient's colour stops in their specified order. */
    public val stops: List<TextColor> =
        stops.toList().also { copiedStops ->
            require(copiedStops.size >= 2) {
                "A color gradient requires at least 2 stops."
            }
        }

    /**
     * Returns the colour at [progress] along the gradient.
     *
     * @param progress the position, where `0f` is the first stop and `1f` is the last. The function limits values
     *   outside `0f..1f` to that range.
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
 * Creates a [ColorGradient] from two or more colour stops.
 *
 * This is the public construction entry for [ColorGradient].
 *
 * @sample io.github.lmliam.kotventure.core.color.gradientSample
 *
 * @throws IllegalArgumentException if fewer than two stops are supplied.
 */
public fun gradient(vararg stops: TextColor): ColorGradient = ColorGradient(stops.asList())

/**
 * Creates a [ColorGradient] from an iterable of two or more colour stops.
 *
 * This is the public construction entry for [ColorGradient] when stops are already a collection.
 *
 * @throws IllegalArgumentException if fewer than two stops are supplied.
 */
public fun gradient(stops: Iterable<TextColor>): ColorGradient = ColorGradient(stops)

/**
 * Builds a component that spreads [stops] across [value] and colours one child for each code point.
 *
 * @sample io.github.lmliam.kotventure.core.color.gradientTextSample
 *
 * @param value the text to colour. The function colours code points, not `Char` values, to keep surrogate pairs intact.
 * @throws IllegalArgumentException if fewer than two stops are supplied.
 */
public fun gradientText(
    value: String,
    vararg stops: TextColor,
): Component = gradientText(value, gradient(*stops))

/**
 * Builds a component that spreads [gradient] across [value] and colours one child for each code point.
 *
 * @param value the text to colour. The function colours code points, not `Char` values, to keep surrogate pairs intact.
 */
public fun gradientText(
    value: String,
    gradient: ColorGradient,
): Component {
    val children = gradientTextChildren(value, gradient)
    if (children.isEmpty()) {
        return emptyComponent()
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
