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

    val typedArrayKind =
        SnbtTypedArrayKind
            .fromPrefix(peek())
        ?.takeIf { peek(1) == ';' }

    if (typedArrayKind != null) {
        skip()
        skip()
        readSnbtElements(']') { validateSnbtTypedArrayValue(typedArrayKind) }
    } else {
        readSnbtElements(']') { validateSnbtValue() }
    }
}

/**
 * Validates one typed-array element at its source offset.
 */
private fun SelectorReader.validateSnbtTypedArrayValue(kind: SnbtTypedArrayKind) {
    val valueOffset = offset
    val value = readSnbtUnquotedScalar()

    if (!kind.accepts(value)) {
        failAt(valueOffset, "Invalid ${kind.prefix} array value '$value'")
    }
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
                .ifEmpty { fail("Expected SNBT compound key") }
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

    while (true) {
        val character = peek() ?: break

        when {
            character.isSnbtScalarTerminator() -> break
            character.isAllowedInUnquotedSelectorToken() -> skip()
            else -> fail("Invalid unquoted SNBT token")
        }
    }

    return substringFrom(start).ifEmpty { fail("Expected SNBT value") }
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

private enum class SnbtTypedArrayKind(
    val prefix: Char,
) {
    BYTE('B') {
        override fun accepts(value: String): Boolean =
            value.endsWith('b', ignoreCase = true) && value.dropLast(1).toByteOrNull() != null
    },

    INT('I') {
        override fun accepts(value: String): Boolean = value.toIntOrNull() != null
    },

    LONG('L') {
        override fun accepts(value: String): Boolean =
            value.endsWith('l', ignoreCase = true) && value.dropLast(1).toLongOrNull() != null
    }, ;

    abstract fun accepts(value: String): Boolean

    companion object {
        fun fromPrefix(prefix: Char?): SnbtTypedArrayKind? = entries.firstOrNull { it.prefix == prefix }
    }
}
