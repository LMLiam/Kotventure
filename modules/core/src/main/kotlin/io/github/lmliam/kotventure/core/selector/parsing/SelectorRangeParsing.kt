package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.SelectorIntRange
import io.github.lmliam.kotventure.core.selector.SelectorRange
import io.github.lmliam.kotventure.core.selector.SelectorRangeArgument

/** Reads and validates a floating-point range for [argument]. */
internal fun SelectorReader.readDoubleRange(argument: SelectorRangeArgument): SelectorRange {
    val bounds = readRangeBounds { readDoubleBound() }
    if (argument.hasNonNegativeOrderedBounds) {
        bounds.requireNonNegative(argument.argumentName, zero = 0.0)
        bounds.requireOrdered(argument.argumentName)
    }
    return SelectorRange(bounds.minimum, bounds.maximum)
}

/**
 * Reads and validates an integer range.
 *
 * @param argumentName The argument name for diagnostics.
 * @param nonNegative Whether bounds must be non-negative.
 */
internal fun SelectorReader.readIntRange(
    argumentName: String,
    nonNegative: Boolean,
): SelectorIntRange {
    val bounds = readRangeBounds { readIntBound() }
    if (nonNegative) bounds.requireNonNegative(argumentName, zero = 0)
    bounds.requireOrdered(argumentName)
    return SelectorIntRange(bounds.minimum, bounds.maximum)
}

/**
 * Reads one optional range bound and gives its source offset to [parser].
 */
private inline fun <T> SelectorReader.readBound(parser: (String, Int) -> T?): T? {
    val start = offset
    val token = readRangeBoundToken()
    return if (token.isEmpty()) null else parser(token, start)
}

private fun SelectorReader.readIntBound(): Int? = readBound(::parseSelectorInt)

private fun SelectorReader.readDoubleBound(): Double? = readBound(::parseSelectorDouble)
