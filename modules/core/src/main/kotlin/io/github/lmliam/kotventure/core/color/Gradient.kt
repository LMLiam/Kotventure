package io.github.lmliam.kotventure.core.color

import io.github.lmliam.kotventure.core.component.emptyComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import kotlin.math.floor
import kotlin.math.min
import kotlin.streams.asSequence

/**
 * An immutable colour gradient with an ordered list of two or more [TextColor] stops.
 *
 * The constructor copies [stops], so later changes to the input collection do not change the gradient. Use [gradient]
 * to create an instance.
 *
 * @throws IllegalArgumentException when fewer than two stops are supplied.
 */
public class ColorGradient internal constructor(
    stops: Iterable<TextColor>,
) {
    /** An immutable copy of the colour stops in interpolation order. */
    public val stops: List<TextColor> = stops.toGradientStops()

    /**
     * Returns the interpolated colour at [progress].
     *
     * @param progress the position, where `0f` is the first stop and `1f` is the last. The function limits values
     *   outside `0f..1f` to that range.
     * @throws IllegalArgumentException when [progress] is not finite.
     */
    public fun colorAt(progress: Float): TextColor {
        require(progress.isFinite()) {
            "Gradient progress must be finite, but was <$progress>."
        }

        val scaledProgress = progress.coerceIn(0f, 1f) * stops.lastIndex
        val startIndex =
            floor(scaledProgress)
                .toInt()
            .coerceAtMost(stops.lastIndex - 1)
        val segmentProgress = scaledProgress - startIndex

        return interpolate(
            progress = segmentProgress,
            start = stops[startIndex],
            end = stops[startIndex + 1],
        )
    }
}

/**
 * Creates a [ColorGradient] from two or more colour stops.
 *
 * @sample io.github.lmliam.kotventure.core.color.gradientSample
 *
 * @throws IllegalArgumentException when fewer than two stops are supplied.
 */
public fun gradient(vararg stops: TextColor): ColorGradient = ColorGradient(stops.asList())

/**
 * Creates a [ColorGradient] from an iterable of two or more colour stops.
 *
 * The function copies [stops]. Later changes to the iterable do not change the result.
 *
 * @throws IllegalArgumentException when fewer than two stops are supplied.
 */
public fun gradient(stops: Iterable<TextColor>): ColorGradient = ColorGradient(stops)

/**
 * Creates a component with one coloured child for each Unicode code point in [value].
 *
 * An empty value returns [emptyComponent]. A one-code-point value uses the first stop. Longer values use the first and
 * last stops at their endpoints and interpolate the other code points.
 *
 * @sample io.github.lmliam.kotventure.core.color.gradientTextSample
 *
 * @param value the text to colour. The function colours code points, not `Char` values, to keep surrogate pairs intact.
 * @throws IllegalArgumentException when fewer than two stops are supplied.
 */
public fun gradientText(
    value: String,
    vararg stops: TextColor,
): Component = gradientText(value, gradient(*stops))

/**
 * Creates a component with one child per Unicode code point and colours it with [gradient].
 *
 * An empty value returns [emptyComponent]. A one-code-point value uses the first stop. Longer values use the first and
 * last stops at their endpoints and interpolate the other code points.
 *
 * @param value the text to colour. The function colours code points, not `Char` values, to keep surrogate pairs intact.
 */
public fun gradientText(
    value: String,
    gradient: ColorGradient,
): Component {
    if (value.isEmpty()) return emptyComponent()

    val codePoints = value.toCodePointStrings()
    val lastIndex = codePoints.lastIndex.toFloat()
    val builder = Component.text()

    codePoints.forEachIndexed { index, text ->
        val progress = if (codePoints.size == 1) 0f else index / lastIndex
        builder.append(Component.text(text).color(gradient.colorAt(progress)))
    }

    return builder.build()
}

private fun Iterable<TextColor>.toGradientStops(): List<TextColor> =
    toList().also { stops ->
        require(stops.size >= 2) {
            "A color gradient requires at least 2 stops."
        }
    }

private fun String.toCodePointStrings(): List<String> =
    codePoints()
        .asSequence()
        .map { codePoint -> String(Character.toChars(codePoint)) }
        .toList()
