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
 * Vanilla integers are an optional `-` followed by ASCII digits. `toIntOrNull` alone is looser:
 * it also accepts a leading `+` and non-ASCII digit characters.
 */
private fun String.isSelectorInteger(): Boolean {
    val digits = removePrefix("-")
    return digits.isNotEmpty() && digits.all(Char::isAsciiDigit)
}

/**
 * Vanilla decimals are an optional `-`, ASCII digits, and at most one `.` (`3`, `-2.5`, `.5`,
 * `1.`). `toDoubleOrNull` alone is looser: it also accepts exponents, hex forms, `Infinity`,
 * and `NaN`.
 */
private fun String.isSelectorDecimal(): Boolean {
    val digits = removePrefix("-").replaceFirst(".", "")
    return digits.isNotEmpty() && digits.all(Char::isAsciiDigit)
}

private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'
