package io.github.lmliam.kotventure.core.selector

internal class SelectorMapParser(
    private val source: String,
    private val sourceOffset: Int,
    private val entryName: String,
) {
    private var offset: Int = 0

    fun <T> parse(transform: (String, Int, String, Int) -> T): List<T> {
        expect('{')
        val entries = mutableListOf<T>()
        if (peek() == '}') {
            offset++
            requireEnd()
            return entries
        }
        while (true) {
            val keyOffset = offset
            val key = readKey()
            if (key.isEmpty()) {
                failAt(keyOffset, "Expected $entryName")
            }
            expect('=')
            val valueOffset = offset
            val value = readValue()
            if (value.isEmpty()) {
                failAt(valueOffset, "Expected value for $entryName '$key'")
            }
            entries += transform(key, sourceOffset + keyOffset, value, sourceOffset + valueOffset)
            when (peek()) {
                ',' -> {
                    offset++
                    if (peek() == '}') {
                        failAt(offset, "Expected $entryName")
                    }
                }
                '}' -> {
                    offset++
                    requireEnd()
                    return entries
                }
                else -> failAt(offset, "Expected ',' or '}'")
            }
        }
    }

    private fun readKey(): String {
        val start = offset
        while (peek()?.let { it != '=' && it != ',' && it != '}' } == true) {
            offset++
        }
        return source.substring(start, offset)
    }

    private fun readValue(): String {
        val start = offset
        val delimiters = ArrayDeque<Char>()
        var quote: Char? = null
        var quoteOffset = -1
        while (offset < source.length) {
            val character = source[offset]
            if (quote != null) {
                if (character == '\\') {
                    offset++
                } else if (character == quote) {
                    quote = null
                }
                offset++
                continue
            }
            when (character) {
                '\'', '"' -> {
                    quote = character
                    quoteOffset = offset
                }
                '{' -> delimiters.addLast('}')
                '[' -> delimiters.addLast(']')
                '}', ']' ->
                    if (delimiters.isEmpty()) {
                        if (character == '}') break
                        failAt(offset, "Unexpected '$character'")
                    } else if (delimiters.removeLast() != character) {
                        failAt(offset, "Mismatched '$character'")
                    }
                ',' -> if (delimiters.isEmpty()) break
            }
            offset++
        }
        if (quote != null) failAt(quoteOffset, "Unterminated quoted string")
        if (delimiters.isNotEmpty()) failAt(offset, "Expected '${delimiters.last()}'")
        return source.substring(start, offset)
    }

    private fun expect(expected: Char) {
        if (peek() != expected) failAt(offset, "Expected '$expected'")
        offset++
    }

    private fun requireEnd() {
        if (offset != source.length) failAt(offset, "Unexpected trailing content")
    }

    private fun peek(): Char? = source.getOrNull(offset)

    private fun failAt(
        localOffset: Int,
        message: String,
    ): Nothing = fail(sourceOffset + localOffset, message)
}
