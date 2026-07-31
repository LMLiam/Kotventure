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
