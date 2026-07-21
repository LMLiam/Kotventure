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

/** Accepts an optional minus sign followed by one or more ASCII digits. */
private fun String.isSelectorInteger(): Boolean {
    val digits = removePrefix("-")
    return digits.isNotEmpty() && digits.all(Char::isAsciiDigit)
}

/** Accepts a plain decimal with an optional minus sign and no exponent. */
private fun String.isSelectorDecimal(): Boolean {
    val withoutSign = removePrefix("-")
    val digits = withoutSign.split('.', limit = 2).joinToString("")
    return digits.isNotEmpty() && digits.all(Char::isAsciiDigit)
}

private fun Char.isAsciiDigit(): Boolean = this in '0'..'9'
