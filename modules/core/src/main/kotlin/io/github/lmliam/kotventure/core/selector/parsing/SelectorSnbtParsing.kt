package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.isAllowedInUnquotedSelectorToken

/**
 * Validates one SNBT compound at the cursor, consuming exactly the compound's source.
 *
 * This validator accepts Java Edition 26.2 container forms. These include trailing commas in compounds, lists, and
 * typed arrays. Lists can contain different element types.
 */
internal fun SelectorReader.validateSnbtCompound() {
    expect('{')
    readSnbtElements('}') {
        validateSnbtCompoundKey()
        skipSnbtWhitespace()
        expect(':')
        skipSnbtWhitespace()
        validateSnbtValue()
    }
}

/**
 * Reads comma-separated elements through [closingDelimiter]. It accepts empty containers and trailing commas.
 */
private inline fun SelectorReader.readSnbtElements(
    closingDelimiter: Char,
    readElement: SelectorReader.() -> Unit,
) {
    skipSnbtWhitespace()
    if (consume(closingDelimiter)) return
    while (true) {
        readElement()
        skipSnbtWhitespace()
        if (consume(closingDelimiter)) return
        expect(',')
        skipSnbtWhitespace()
        if (consume(closingDelimiter)) return
    }
}

/**
 * Validates and consumes one SNBT value at the cursor.
 */
private fun SelectorReader.validateSnbtValue() {
    when (peek()) {
        '{' -> validateSnbtCompound()
        '[' -> validateSnbtListOrArray()
        '\'', '"' -> readQuotedString()
        null -> fail("Expected SNBT value")
        else -> readSnbtUnquotedScalar()
    }
}

/**
 * Validates a list or typed array. A typed array starts with `B;`, `I;`, or `L;`.
 */
private fun SelectorReader.validateSnbtListOrArray() {
    expect('[')
    skipSnbtWhitespace()
    val arrayType = peek()
    if (arrayType != null && arrayType in SNBT_TYPED_ARRAY_PREFIXES && peek(1) == ';') {
        skip()
        skip()
        readSnbtElements(']') { validateSnbtTypedArrayValue(arrayType) }
        return
    }
    readSnbtElements(']') { validateSnbtValue() }
}

/**
 * Validates one typed-array element at its source offset.
 */
private fun SelectorReader.validateSnbtTypedArrayValue(arrayType: Char) {
    val valueOffset = offset
    val value = readSnbtUnquotedScalar()
    if (!isValidSnbtTypedArrayValue(value, arrayType)) {
        failAt(valueOffset, "Invalid $arrayType array value '$value'")
    }
}

/**
 * Checks the suffix and numeric range of one typed-array element.
 */
private fun isValidSnbtTypedArrayValue(
    value: String,
    arrayType: Char,
): Boolean =
    when (arrayType) {
        'B' -> value.endsWith('b', ignoreCase = true) && value.dropLast(1).toByteOrNull() != null
        'I' -> value.toIntOrNull() != null
        'L' -> value.endsWith('l', ignoreCase = true) && value.dropLast(1).toLongOrNull() != null
        else -> false
    }

/**
 * Validates one quoted or unquoted compound key.
 */
private fun SelectorReader.validateSnbtCompoundKey() {
    when (peek()) {
        '\'', '"' -> readQuotedString()
        null -> fail("Expected SNBT compound key")
        else ->
            readWhile(Char::isAllowedInUnquotedSelectorToken)
                .let { if (it.isEmpty()) fail("Expected SNBT compound key") }
    }
}

/**
 * Reads an unquoted SNBT scalar.
 *
 * The scalar ends at a comma, closing delimiter, or whitespace. It must contain at least one permitted character.
 *
 * @return The unquoted scalar.
 */
private fun SelectorReader.readSnbtUnquotedScalar(): String {
    val start = offset
    while (peek()?.let { !it.isSnbtScalarTerminator() && it.isAllowedInUnquotedSelectorToken() } == true) {
        skip()
    }
    if (peek()?.let { !it.isSnbtScalarTerminator() } == true) {
        fail("Invalid unquoted SNBT token")
    }
    return substringFrom(start).also {
        if (it.isEmpty()) fail("Expected SNBT value")
    }
}

/**
 * Returns whether this character terminates an unquoted SNBT scalar.
 */
private fun Char.isSnbtScalarTerminator(): Boolean = this in ",]}" || isWhitespace()

/**
 * Skips contiguous whitespace at the cursor.
 */
private fun SelectorReader.skipSnbtWhitespace() {
    while (peek()?.isWhitespace() == true) skip()
}

private val SNBT_TYPED_ARRAY_PREFIXES: Set<Char> = setOf('B', 'I', 'L')
