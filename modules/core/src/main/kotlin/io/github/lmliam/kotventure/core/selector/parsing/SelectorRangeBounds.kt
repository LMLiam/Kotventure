package io.github.lmliam.kotventure.core.selector.parsing

/** The vanilla range separator, as in `level=1..30`. */
private const val RANGE_SEPARATOR: String = ".."

/**
 * The bounds of one parsed range, carrying each bound's source offset so validation failures
 * point at the offending bound.
 */
internal class SelectorRangeBounds<T : Comparable<T>>(
    private val reader: SelectorReader,
    val minimum: T?,
    private val minimumOffset: Int,
    val maximum: T?,
    private val maximumOffset: Int,
) {
    fun requireNonNegative(
        argument: String,
        zero: T,
    ) {
        if (minimum != null && minimum < zero) {
            reader.failAt(minimumOffset, "'$argument' bounds must be non-negative")
        }
        if (maximum != null && maximum < zero) {
            reader.failAt(maximumOffset, "'$argument' bounds must be non-negative")
        }
    }

    fun requireOrdered(argument: String) {
        if (minimum != null && maximum != null && minimum > maximum) {
            reader.failAt(maximumOffset, "'$argument' minimum must not exceed maximum")
        }
    }
}

internal fun <T : Comparable<T>> SelectorReader.readRangeBounds(
    readBound: SelectorReader.() -> T?,
): SelectorRangeBounds<T> {
    val minimumOffset = offset
    val minimum = readBound()
    if (!consume(RANGE_SEPARATOR)) {
        if (minimum == null) failAt(minimumOffset, "Expected a range")
        return SelectorRangeBounds(this, minimum, minimumOffset, minimum, minimumOffset)
    }
    if (peek() == '.') fail("Range contains more than one '$RANGE_SEPARATOR' separator")
    val maximumOffset = offset
    val maximum = readBound()
    if (minimum == null && maximum == null) {
        failAt(minimumOffset, "Range must contain at least one bound")
    }
    return SelectorRangeBounds(this, minimum, minimumOffset, maximum, maximumOffset)
}

/** Reads a bound token, stopping at value delimiters and at the [RANGE_SEPARATOR]. */
internal fun SelectorReader.readRangeBoundToken(): String {
    val start = offset
    while (true) {
        val character = peek() ?: break
        if (character == ',' || character == ']' || character == '}') break
        if (character == '.' && peek(1) == '.') break
        skip()
    }
    return substringFrom(start)
}
