package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.isAllowedInUnquotedSelectorToken

/**
 * Validates one SNBT compound at the cursor, consuming exactly the compound's source.
 *
 * Java Edition 26.2 container forms are accepted: trailing commas in compounds, lists, and typed
 * arrays, plus heterogeneous list elements.
 */
internal fun SelectorReader.validateSnbtCompound() {
    expect('{')
    skipSnbtWhitespace()
    if (consume('}')) return
    while (true) {
        validateSnbtCompoundKey()
        skipSnbtWhitespace()
        expect(':')
        skipSnbtWhitespace()
        validateSnbtValue()
        skipSnbtWhitespace()
        if (consume('}')) return
        expect(',')
        skipSnbtWhitespace()
        if (consume('}')) return
    }
}

private fun SelectorReader.validateSnbtValue() {
    when (peek()) {
        '{' -> validateSnbtCompound()
        '[' -> validateSnbtListOrArray()
        '\'', '"' -> readQuotedString()
        null -> fail("Expected SNBT value")
        else -> readSnbtUnquotedScalar()
    }
}

private fun SelectorReader.validateSnbtListOrArray() {
    expect('[')
    skipSnbtWhitespace()
    val arrayType = peek()
    if (arrayType != null && arrayType in SNBT_TYPED_ARRAY_PREFIXES && peekSecond() == ';') {
        skip()
        skip()
        validateSnbtTypedArrayValues(arrayType)
        return
    }
    if (consume(']')) return
    while (true) {
        validateSnbtValue()
        skipSnbtWhitespace()
        if (consume(']')) return
        expect(',')
        skipSnbtWhitespace()
        if (consume(']')) return
    }
}

private fun SelectorReader.validateSnbtTypedArrayValues(arrayType: Char) {
    skipSnbtWhitespace()
    if (consume(']')) return
    while (true) {
        val valueOffset = offset
        val value = readSnbtUnquotedScalar()
        if (!isValidSnbtTypedArrayValue(value, arrayType)) {
            failAt(valueOffset, "Invalid $arrayType array value '$value'")
        }
        skipSnbtWhitespace()
        if (consume(']')) return
        expect(',')
        skipSnbtWhitespace()
        if (consume(']')) return
    }
}

private fun isValidSnbtTypedArrayValue(
    value: String,
    arrayType: Char,
): Boolean =
    when (arrayType) {
        'B' -> SNBT_BYTE.matches(value) && value.dropLast(1).toByteOrNull() != null
        'I' -> SNBT_INT.matches(value) && value.toIntOrNull() != null
        'L' -> SNBT_LONG.matches(value) && value.dropLast(1).toLongOrNull() != null
        else -> false
    }

private fun SelectorReader.validateSnbtCompoundKey() {
    when (peek()) {
        '\'', '"' -> readQuotedString()
        null -> fail("Expected SNBT compound key")
        else -> {
            val key = readWhile(Char::isAllowedInUnquotedSelectorToken)
            if (key.isEmpty()) fail("Expected SNBT compound key")
        }
    }
}

private fun SelectorReader.readSnbtUnquotedScalar(): String {
    val start = offset
    while (true) {
        val character = peek() ?: break
        if (character == ',' || character == '}' || character == ']' || character.isWhitespace()) break
        if (!character.isAllowedInUnquotedSelectorToken()) fail("Invalid unquoted SNBT token")
        skip()
    }
    if (offset == start) fail("Expected SNBT value")
    return substringFrom(start)
}

private fun SelectorReader.skipSnbtWhitespace() {
    while (peek()?.isWhitespace() == true) skip()
}

private val SNBT_BYTE: Regex = Regex("[+-]?[0-9]+[bB]")
private val SNBT_INT: Regex = Regex("[+-]?[0-9]+")
private val SNBT_LONG: Regex = Regex("[+-]?[0-9]+[lL]")
private val SNBT_TYPED_ARRAY_PREFIXES: Set<Char> = setOf('B', 'I', 'L')
