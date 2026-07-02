package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.isAllowedInUnquotedSelectorToken

/** Reads until the next selector value delimiter (`,`, `]`, or `}`). */
internal fun SelectorReader.readValueToken(): String = readWhile { it != ',' && it != ']' && it != '}' }

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
 * Callers establish the opening quote by peeking before calling.
 */
internal fun SelectorReader.readQuotedString(): String {
    val quoteOffset = offset
    val quote = peek()?.takeIf { it == '\'' || it == '"' }
    checkNotNull(quote) { "readQuotedString requires the cursor to be on a quote" }
    skip()
    val decoded = StringBuilder()
    while (true) {
        val characterOffset = offset
        val character = peek() ?: failAt(quoteOffset, "Unterminated quoted string")
        skip()
        when (character) {
            quote -> return decoded.toString()
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
