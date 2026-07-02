package io.github.lmliam.kotventure.core.selector

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

    fun peek(): Char? = source.getOrNull(offset)

    fun peekSecond(): Char? = source.getOrNull(offset + 1)

    fun skip() {
        offset++
    }

    fun consume(expected: Char): Boolean {
        if (peek() != expected) return false
        offset++
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
    ): Nothing = throw EntitySelectorParseException(offset, message)
}
