package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.isAllowedInUnquotedSelectorToken

/** Reads until the next selector value delimiter (`,`, `]`, or `}`). */
internal fun SelectorReader.readValueToken(): String = readWhile { it !in ",]}" }

/**
 * Validates an unquoted selector token.
 *
 * The token must be non-empty and contain only characters that Brigadier permits in an unquoted string.
 *
 * @param value The token to validate.
 * @param valueOffset The source offset at which the token starts.
 * @param description The token description for the diagnostic.
 */
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
 * Reads and decodes a string delimited by `'` or `"`.
 *
 * A backslash can escape a single quote, a double quote, or another backslash. The cursor must be on the opening
 * quote. The returned string does not include the delimiters.
 *
 * @return The decoded string without delimiters.
 * @throws io.github.lmliam.kotventure.core.selector.EntitySelectorParseException when the string is unterminated or
 * contains an invalid escape.
 */
internal fun SelectorReader.readQuotedString(): String {
    val quoteOffset = offset
    val quote = peek()?.takeIf { it in "'\"" } ?: error("readQuotedString requires the cursor to be on a quote")
    skip()

    return buildString {
        while (true) {
            val characterOffset = offset
            val character = peek() ?: failAt(quoteOffset, "Unterminated quoted string")
            skip()
            when (character) {
                quote -> return@buildString
                '\\' -> append(handleEscape(quoteOffset, characterOffset))
                else -> append(character)
            }
        }
    }
}

/**
 * Reads one quoted-string escape and reports an error at the opening quote or backslash.
 */
private fun SelectorReader.handleEscape(
    quoteOffset: Int,
    escapeOffset: Int,
): Char {
    val escaped = peek() ?: failAt(quoteOffset, "Unterminated quoted string")
    if (escaped !in "\"'\\") failAt(escapeOffset, "Invalid quoted-string escape")
    skip()
    return escaped
}
