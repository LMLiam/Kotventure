package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.SelectorIntRange
import io.github.lmliam.kotventure.core.selector.SelectorRange
import io.github.lmliam.kotventure.core.selector.SelectorRangeArgument

internal fun SelectorReader.readDoubleRange(argument: SelectorRangeArgument): SelectorRange {
    val bounds = readRangeBounds { readDoubleBound() }
    if (argument.hasNonNegativeOrderedBounds) {
        bounds.requireNonNegative(argument.argumentName, zero = 0.0)
        bounds.requireOrdered(argument.argumentName)
    }
    return SelectorRange(bounds.minimum, bounds.maximum)
}

internal fun SelectorReader.readIntRange(
    argument: String,
    nonNegative: Boolean,
): SelectorIntRange {
    val bounds = readRangeBounds { readIntBound() }
    if (nonNegative) bounds.requireNonNegative(argument, zero = 0)
    bounds.requireOrdered(argument)
    return SelectorIntRange(bounds.minimum, bounds.maximum)
}

private fun SelectorReader.readIntBound(): Int? {
    val start = offset
    val token = readRangeBoundToken()
    if (token.isEmpty()) return null
    return parseSelectorInt(token, start)
}

private fun SelectorReader.readDoubleBound(): Double? {
    val start = offset
    val token = readRangeBoundToken()
    if (token.isEmpty()) return null
    return parseSelectorDouble(token, start)
}
