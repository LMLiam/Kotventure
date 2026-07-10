package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.isAllowedInUnquotedSelectorToken

/** Reads until the next selector value delimiter (`,`, `]`, or `}`). */
internal fun SelectorReader.readValueToken(): String = readWhile { it !in ",]}" }

/**
 * Validate an unquoted token for correct characters.
 *
 * The token must be non-empty and all characters must be allowed in unquoted selector
tokens.
 * Fails at [valueOffset] if validation fails.
 *
 * @param value the token string to validate
 * @param valueOffset the cursor offset when the token began (for error reporting)
 * @param description a brief noun phrase describing the token (e.g., "sort value")
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
 * Reads and decodes a `'`- or `"`-delimited string; only the delimiter and `\` may be
escaped.
 *
 * Callers must establish the opening quote by peeking before calling; the quote
character will
 * be consumed and the string returned without delimiters.
 *
 * @return the decoded string (without delimiters)
 * @throws io.github.lmliam.kotventure.core.selector.EntitySelectorParseException if the
string is
 * unterminated or contains invalid escapes
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
 * Handle an escape sequence in a quoted string.
 *
 * Valid escapes are the quote character itself or a backslash. Any other escape is
invalid.
 *
 * @param quoteOffset the offset of the opening quote (for unterminated-string errors)
 * @param escapeOffset the offset of the backslash (for invalid-escape errors)
 * @return the escaped character to append to the decoded string
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
