package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.SelectorIntRange
import io.github.lmliam.kotventure.core.selector.SelectorRange
import io.github.lmliam.kotventure.core.selector.SelectorRangeArgument

/**
 * Read a double range for a selector argument.
 */
internal fun SelectorReader.readDoubleRange(argument: SelectorRangeArgument): SelectorRange {
    val bounds = readRangeBounds { readDoubleBound() }
    if (argument.hasNonNegativeOrderedBounds) {
        bounds.requireNonNegative(argument.argumentName, zero = 0.0)
        bounds.requireOrdered(argument.argumentName)
    }
    return SelectorRange(bounds.minimum, bounds.maximum)
}

/**
 * Read an int range for a selector argument.
 *
 * @param argumentName the name of the selector argument (used for error messages)
 * @param nonNegative whether bounds must be non-negative
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
 * Generic helper that reads a range-bound token and parses it with [parser].
 * Captures the reader offset before token reading so parsers can report accurate positions.
 */
private inline fun <T> SelectorReader.readBound(parser: (String, Int) -> T?): T? {
    val start = offset
    val token = readRangeBoundToken()
    return if (token.isEmpty()) null else parser(token, start)
}

private fun SelectorReader.readIntBound(): Int? = readBound(::parseSelectorInt)

private fun SelectorReader.readDoubleBound(): Double? = readBound(::parseSelectorDouble)
