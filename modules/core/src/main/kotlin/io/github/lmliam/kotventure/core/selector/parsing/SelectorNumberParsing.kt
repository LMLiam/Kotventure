package io.github.lmliam.kotventure.core.selector.parsing

internal fun SelectorReader.readSelectorBoolean(): Boolean {
    val start = offset
    return when (readValueToken()) {
        "true" -> true
        "false" -> false
        else -> failAt(start, "Expected 'true' or 'false'")
    }
}

internal fun SelectorReader.readSelectorInt(): Int {
    val start = offset
    return parseSelectorInt(readValueToken(), start)
}

internal fun SelectorReader.parseSelectorInt(
    value: String,
    valueOffset: Int,
): Int {
    if (!SELECTOR_INTEGER.matches(value)) failAt(valueOffset, "Expected an integer")
    return value.toIntOrNull() ?: failAt(valueOffset, "Integer is outside the supported range")
}

internal fun SelectorReader.readSelectorDouble(): Double {
    val start = offset
    return parseSelectorDouble(readValueToken(), start)
}

internal fun SelectorReader.parseSelectorDouble(
    value: String,
    valueOffset: Int,
): Double {
    if (!SELECTOR_DOUBLE.matches(value)) failAt(valueOffset, "Expected a finite decimal number")
    return value.toDoubleOrNull()?.takeIf(Double::isFinite)
        ?: failAt(valueOffset, "Expected a finite decimal number")
}

private val SELECTOR_DOUBLE: Regex = Regex("-?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)")
private val SELECTOR_INTEGER: Regex = Regex("-?[0-9]+")
