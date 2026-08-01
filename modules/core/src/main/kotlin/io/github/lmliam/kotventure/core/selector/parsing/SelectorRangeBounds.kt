package io.github.lmliam.kotventure.core.selector.parsing

/** The vanilla range separator, as in `level=1..30`. */
private const val RANGE_SEPARATOR = ".."

/**
 * Holds parsed bounds and their source offsets for precise validation errors.
 */
internal class SelectorRangeBounds<T : Comparable<T>> private constructor(
    private val reader: SelectorReader,
    private val lower: Bound<T>?,
    private val upper: Bound<T>?,
) {
    val minimum: T?
        get() = lower?.value

    val maximum: T?
        get() = upper?.value

    fun requireNonNegative(
        argument: String,
        zero: T,
    ) {
        if (lower != null && lower.value < zero) {
            reader.failAt(lower.offset, "'$argument' bounds must be non-negative")
        }

        if (upper != null && upper.value < zero) {
            reader.failAt(upper.offset, "'$argument' bounds must be non-negative")
        }
    }

    fun requireOrdered(argument: String) {
        val lower = lower ?: return
        val upper = upper ?: return

        if (lower.value > upper.value) {
            reader.failAt(upper.offset, "'$argument' minimum must not exceed maximum")
        }
    }

    private data class Bound<T>(
        val value: T,
        val offset: Int,
    )

    companion object {
        fun <T : Comparable<T>> exact(
            reader: SelectorReader,
            value: T,
            offset: Int,
        ): SelectorRangeBounds<T> =
            Bound(value, offset).let { bound ->
                SelectorRangeBounds(
                    reader = reader,
                    lower = bound,
                    upper = bound,
                )
            }

        fun <T : Comparable<T>> ranged(
            reader: SelectorReader,
            minimum: T?,
            minimumOffset: Int,
            maximum: T?,
            maximumOffset: Int,
        ): SelectorRangeBounds<T> =
            SelectorRangeBounds(
                reader = reader,
                lower = minimum?.let { Bound(it, minimumOffset) },
                upper = maximum?.let { Bound(it, maximumOffset) },
            )
    }
}

internal fun <T : Comparable<T>> SelectorReader.readRangeBounds(
    readBound: SelectorReader.() -> T?,
): SelectorRangeBounds<T> {
    val minimumOffset = offset
    val minimum = readBound()

    if (!consume(RANGE_SEPARATOR)) {
        return SelectorRangeBounds.exact(
            reader = this,
            value = minimum ?: failAt(minimumOffset, "Expected a range"),
            offset = minimumOffset,
        )
    }

    if (peek() == '.') {
        fail("Range contains more than one '$RANGE_SEPARATOR' separator")
    }

    val maximumOffset = offset
    val maximum = readBound()

    if (minimum == null && maximum == null) {
        failAt(minimumOffset, "Range must contain at least one bound")
    }

    return SelectorRangeBounds.ranged(
        reader = this,
        minimum = minimum,
        minimumOffset = minimumOffset,
        maximum = maximum,
        maximumOffset = maximumOffset,
    )
}

/** Reads a bound token up to a value delimiter or [RANGE_SEPARATOR]. */
internal fun SelectorReader.readRangeBoundToken(): String {
    val start = offset

    while (true) {
        when (peek()) {
            null, ',', ']', '}' -> break
            '.' -> if (peek(1) == '.') break else skip()
            else -> skip()
        }
    }

    return substringFrom(start)
}
