package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelectorParseException

/**
 * A character cursor over entity-selector source text.
 *
 * Every value parses in place, so each diagnostic offset is the exact cursor position at the
 * failure - parsers built on this reader never reconstruct offsets arithmetically.
 */
internal class SelectorReader(
    private val source: String,
) {
    /** Zero-based index of the next unread character. */
    var offset: Int = 0
        private set

    fun isAtEnd(): Boolean = offset == source.length

    /** Returns the character [distance] positions ahead of the cursor without consuming it. */
    @Suppress("NOTHING_TO_INLINE") // inline to avoid allocating the lambda where callers are hot
    inline fun peek(distance: Int = 0): Char? = source.getOrNull(offset + distance)

    fun skip() {
        offset++
    }

    /**
     * Consume a single [expected] char if it matches the next character.
     *
     * Returns true when the character was present and consumed.
     */
    fun consume(expected: Char): Boolean = (peek() == expected).also { if (it) offset++ }

    /**
     * Consumes the whole [expected] token if the source continues with it.
     *
     * Returns true when the token was present and consumed.
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
     * Read characters while [predicate] holds and return the substring from the starting cursor.
     *
     * This is inline to avoid allocating the predicate where callers are hot.
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
