package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelectorParseException

/**
 * A character cursor over entity-selector source text.
 *
 * Every value parses in place, so each diagnostic offset is the exact cursor position at the
 * failure — parsers built on this reader never reconstruct offsets arithmetically.
 */
internal class SelectorReader(
    private val source: String,
) {
    /** Zero-based index of the next unread character. */
    var offset: Int = 0
        private set

    fun isAtEnd(): Boolean = offset == source.length

    /** Returns the character [distance] positions ahead of the cursor without consuming it. */
    fun peek(distance: Int = 0): Char? = source.getOrNull(offset + distance)

    fun skip() {
        offset++
    }

    fun consume(expected: Char): Boolean {
        if (peek() != expected) return false
        offset++
        return true
    }

    /** Consumes the whole [expected] token if the source continues with it. */
    fun consume(expected: String): Boolean {
        if (!source.startsWith(expected, offset)) return false
        offset += expected.length
        return true
    }

    fun expect(
        expected: Char,
        message: String = "Expected '$expected'",
    ) {
        if (peek() != expected) fail(message)
        offset++
    }

    fun readWhile(predicate: (Char) -> Boolean): String {
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
