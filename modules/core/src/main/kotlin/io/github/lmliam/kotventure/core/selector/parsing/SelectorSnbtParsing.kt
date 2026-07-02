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
    if (arrayType != null && arrayType in SNBT_TYPED_ARRAY_PREFIXES && peek(1) == ';') {
        skip()
        skip()
        readSnbtElements(']') { validateSnbtTypedArrayValue(arrayType) }
        return
    }
    readSnbtElements(']') { validateSnbtValue() }
}

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
        if (character.isSnbtScalarTerminator()) break
        if (!character.isAllowedInUnquotedSelectorToken()) fail("Invalid unquoted SNBT token")
        skip()
    }
    if (offset == start) fail("Expected SNBT value")
    return substringFrom(start)
}

private fun Char.isSnbtScalarTerminator(): Boolean = this in ",]}" || isWhitespace()

private fun SelectorReader.skipSnbtWhitespace() {
    while (peek()?.isWhitespace() == true) skip()
}

private val SNBT_TYPED_ARRAY_PREFIXES: Set<Char> = setOf('B', 'I', 'L')
