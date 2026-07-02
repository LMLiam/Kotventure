package io.github.lmliam.kotventure.core.selector

internal fun SelectorReader.readDoubleRange(argument: SelectorRangeArgument): SelectorRange {
    val bounds = readRangeBounds { readDoubleBound() }
    if (argument.hasNonNegativeOrderedBounds) {
        requireNonNegativeBounds(bounds, argument.argumentName, zero = 0.0)
        requireOrderedBounds(bounds, argument.argumentName)
    }
    return SelectorRange(bounds.minimum, bounds.maximum)
}

internal fun SelectorReader.readIntRange(
    argument: String,
    nonNegative: Boolean,
): SelectorIntRange {
    val bounds = readRangeBounds { readIntBound() }
    if (nonNegative) requireNonNegativeBounds(bounds, argument, zero = 0)
    requireOrderedBounds(bounds, argument)
    return SelectorIntRange(bounds.minimum, bounds.maximum)
}

private class SelectorRangeBounds<T : Comparable<T>>(
    val minimum: T?,
    val minimumOffset: Int,
    val maximum: T?,
    val maximumOffset: Int,
)

private fun <T : Comparable<T>> SelectorReader.readRangeBounds(
    readBound: SelectorReader.() -> T?,
): SelectorRangeBounds<T> {
    val minimumOffset = offset
    val minimum = readBound()
    if (!consumeRangeSeparator()) {
        if (minimum == null) failAt(minimumOffset, "Expected a range")
        return SelectorRangeBounds(minimum, minimumOffset, minimum, minimumOffset)
    }
    if (peek() == '.') fail("Range contains more than one '..' separator")
    val maximumOffset = offset
    val maximum = readBound()
    if (minimum == null && maximum == null) {
        failAt(minimumOffset, "Range must contain at least one bound")
    }
    return SelectorRangeBounds(minimum, minimumOffset, maximum, maximumOffset)
}

private fun SelectorReader.consumeRangeSeparator(): Boolean {
    if (peek() != '.' || peekSecond() != '.') return false
    skip()
    skip()
    return true
}

private fun <T : Comparable<T>> SelectorReader.requireNonNegativeBounds(
    bounds: SelectorRangeBounds<T>,
    argument: String,
    zero: T,
) {
    if (bounds.minimum != null && bounds.minimum < zero) {
        failAt(bounds.minimumOffset, "'$argument' bounds must be non-negative")
    }
    if (bounds.maximum != null && bounds.maximum < zero) {
        failAt(bounds.maximumOffset, "'$argument' bounds must be non-negative")
    }
}

private fun <T : Comparable<T>> SelectorReader.requireOrderedBounds(
    bounds: SelectorRangeBounds<T>,
    argument: String,
) {
    val minimum = bounds.minimum ?: return
    val maximum = bounds.maximum ?: return
    if (minimum > maximum) failAt(bounds.maximumOffset, "'$argument' minimum must not exceed maximum")
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

private fun SelectorReader.readRangeBoundToken(): String {
    val start = offset
    while (true) {
        val character = peek() ?: break
        if (character == ',' || character == ']' || character == '}') break
        if (character == '.' && peekSecond() == '.') break
        skip()
    }
    return substringFrom(start)
}
