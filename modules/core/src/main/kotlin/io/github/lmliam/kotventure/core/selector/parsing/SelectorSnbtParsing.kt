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
    readSnbtElements('}') {
        validateSnbtCompoundKey()
        skipSnbtWhitespace()
        expect(':')
        skipSnbtWhitespace()
        validateSnbtValue()
    }
}

/**
 * Reads `,`-separated container elements up to and including [closingDelimiter], accepting empty
 * containers and trailing commas.
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
 * Validate a single SNBT value: compound, list, array, quoted string, or unquoted scalar.
 *
 * Consumes the value from the source at the current cursor.
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
 * Validate a list or typed array. Typed arrays have a single-char prefix (`B`, `I`, `L`)
 * followed by `;`, then heterogeneous or typed elements.
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
 * Validate a single typed-array element and fail with a precise offset if invalid.
 */
private fun SelectorReader.validateSnbtTypedArrayValue(arrayType: Char) {
    val valueOffset = offset
    val value = readSnbtUnquotedScalar()
    if (!isValidSnbtTypedArrayValue(value, arrayType)) {
        failAt(valueOffset, "Invalid $arrayType array value '$value'")
    }
}

/**
 * Typed-array elements are an optionally signed integer, suffixed `b`/`B` for byte arrays and
 * `l`/`L` for long arrays. `toByteOrNull`/`toIntOrNull`/`toLongOrNull` accept exactly that
 * unsuffixed language (and reject overflow), so no separate syntax check is needed — the scalar
 * reader has already restricted the charset.
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
 * Validate a compound key (quoted string or unquoted identifier).
 *
 * Fails if the key is empty or malformed.
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
 * Read an unquoted SNBT scalar (identifier or number).
 *
 * Consumes characters until a terminator (`,`, `]`, `}`, or whitespace) is encountered.
 * Fails if the scalar is empty or contains disallowed characters.
 *
 * @return the unquoted scalar string
 */
private fun SelectorReader.readSnbtUnquotedScalar(): String {
    val start = offset
    while (peek()?.let { !it.isSnbtScalarTerminator() && it.isAllowedInUnquotedSelectorToken() } == true) {
        skip()
    }
    // Check for invalid characters that would have stopped the loop
    if (peek()?.let { !it.isSnbtScalarTerminator() } == true) {
        fail("Invalid unquoted SNBT token")
    }
    return substringFrom(start).also {
        if (it.isEmpty()) fail("Expected SNBT value")
    }
}

/**
 * Check if this character is a terminator for an unquoted SNBT scalar.
 *
 * Terminators are `,`, `]`, `}`, or whitespace.
 */
private fun Char.isSnbtScalarTerminator(): Boolean = this in ",]}" || isWhitespace()

/**
 * Skip over all contiguous whitespace characters at the current cursor.
 */
private fun SelectorReader.skipSnbtWhitespace() {
    while (peek()?.isWhitespace() == true) skip()
}

private val SNBT_TYPED_ARRAY_PREFIXES: Set<Char> = setOf('B', 'I', 'L')
