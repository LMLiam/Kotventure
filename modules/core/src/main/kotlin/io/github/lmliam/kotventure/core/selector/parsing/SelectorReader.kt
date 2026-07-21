package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelectorParseException

/**
 * A cursor over entity-selector source text.
 *
 * Parsing functions capture source positions before they consume values. Diagnostics can therefore identify the
 * position at which a failure occurred.
 */
internal class SelectorReader(
    private val source: String,
) {
    /** Zero-based index of the next unread character. */
    var offset: Int = 0
        private set

    fun isAtEnd(): Boolean = offset == source.length

    /** Returns the character [distance] positions ahead without consuming it. */
    @Suppress("NOTHING_TO_INLINE")
    inline fun peek(distance: Int = 0): Char? = source.getOrNull(offset + distance)

    fun skip() {
        offset++
    }

    /**
     * Consumes [expected] if it is the next character.
     *
     * Returns `true` when the character was present.
     */
    fun consume(expected: Char): Boolean = (peek() == expected).also { if (it) offset++ }

    /**
     * Consumes [expected] if it is the next token.
     *
     * Returns `true` when the token was present.
     */
    fun consume(expected: String): Boolean =
        source.startsWith(expected, offset).also { if (it) offset += expected.length }

    fun expect(
        expected: Char,
        message: String = "Expected '$expected'",
    ) {
        if (peek() != expected) fail(message)
        offset++
    }

    /**
     * Reads and returns characters while [predicate] is `true`.
     */
    inline fun readWhile(predicate: (Char) -> Boolean): String {
        val start = offset
        while (peek()?.let(predicate) == true) offset++
        return substringFrom(start)
    }

    fun substringFrom(start: Int): String = source.substring(start, offset)

    fun fail(message: String): Nothing = failAt(offset, message)

    fun failAt(
        offset: Int,
        message: String,
        cause: Throwable? = null,
    ): Nothing = throw EntitySelectorParseException(offset, message, cause)
}
