package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.parsing.isAsciiDigit

internal fun SelectorReader.readSelectorBoolean(): Boolean {
    val start = offset
    return when (val token = readValueToken()) {
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
    if (!value.isSelectorInteger()) failAt(valueOffset, "Expected an integer")
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
    if (!value.isSelectorDecimal()) failAt(valueOffset, "Expected a finite decimal number")
    return value.toDoubleOrNull()?.takeIf(Double::isFinite)
        ?: failAt(valueOffset, "Expected a finite decimal number")
}

/**
 * Vanilla integers: optional '-' then ASCII digits.
 *
 * `toIntOrNull` is intentionally looser (accepts '+' and non-ASCII digits), so we validate here.
 */
private fun String.isSelectorInteger(): Boolean {
    val digits = removePrefix("-")
    return digits.isNotEmpty() && digits.all(Char::isAsciiDigit)
}

/**
 * Vanilla decimals: optional '-', ASCII digits, and at most one '.'.
 * Examples allowed: "3", "-2.5", ".5", "1."
 *
 * We split on the first '.' and ensure the concatenated digits are all ASCII digits and non-empty.
 */
private fun String.isSelectorDecimal(): Boolean {
    val withoutSign = removePrefix("-")
    val concatenated = withoutSign.split('.', limit = 2).joinToString("")
    return concatenated.isNotEmpty() && concatenated.all(Char::isAsciiDigit)
}

private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'
