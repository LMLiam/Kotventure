package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.InvalidKeyException
import net.kyori.adventure.key.Key

/** A decoded selector string. */
internal class QuotedSelectorString(
    val value: String,
)

/** Reads until the next selector value delimiter (`,`, `]`, or `}`). */
internal fun SelectorReader.readValueToken(): String = readWhile { it != ',' && it != ']' && it != '}' }

internal fun SelectorReader.readSelectorKey(): Key {
    val start = offset
    return parseSelectorKey(readValueToken(), start)
}

internal fun SelectorReader.parseSelectorKey(
    value: String,
    valueOffset: Int,
): Key {
    if (value.isEmpty()) failAt(valueOffset, "Expected a namespaced key")
    return try {
        Key.key(value)
    } catch (_: InvalidKeyException) {
        failAt(valueOffset, "Invalid namespaced key '$value'")
    }
}

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

internal fun SelectorReader.validateUnquotedToken(
    value: String,
    valueOffset: Int,
    description: String = "token",
) {
    if (value.isEmpty() || !value.all(Char::isAllowedInUnquotedSelectorToken)) {
        failAt(valueOffset, "Invalid unquoted selector $description")
    }
}

/**
 * Reads and decodes a `'`- or `"`-delimited string; only the delimiter and `\` may be escaped.
 */
internal fun SelectorReader.readQuotedString(): QuotedSelectorString {
    val quoteOffset = offset
    val quote = peek()?.takeIf { it == '\'' || it == '"' } ?: fail("Expected a quoted string")
    skip()
    val decoded = StringBuilder()
    while (true) {
        val characterOffset = offset
        val character = peek() ?: failAt(quoteOffset, "Unterminated quoted string")
        skip()
        when (character) {
            quote -> return QuotedSelectorString(decoded.toString())
            '\\' -> {
                val escaped = peek() ?: failAt(quoteOffset, "Unterminated quoted string")
                if (escaped != quote && escaped != '\\') {
                    failAt(characterOffset, "Invalid quoted-string escape")
                }
                skip()
                decoded.append(escaped)
            }

            else -> decoded.append(character)
        }
    }
}

private val SELECTOR_DOUBLE: Regex = Regex("-?(?:[0-9]+(?:\\.[0-9]*)?|\\.[0-9]+)")
private val SELECTOR_INTEGER: Regex = Regex("-?[0-9]+")
