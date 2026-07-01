package io.github.lmliam.kotventure.core.selector

internal class SelectorSnbtValidator(
    private val source: String,
    private val sourceOffset: Int,
) {
    private var offset: Int = 0

    fun validateCompound() {
        skipWhitespace()
        parseCompound()
        skipWhitespace()
        if (offset != source.length) failAt(offset, "Unexpected trailing SNBT content")
    }

    private fun parseCompound() {
        expect('{')
        skipWhitespace()
        if (consume('}')) return
        while (true) {
            parseCompoundKey()
            skipWhitespace()
            expect(':')
            skipWhitespace()
            parseValue()
            skipWhitespace()
            if (consume('}')) return
            expect(',')
            skipWhitespace()
            if (peek() == '}') failAt(offset, "Expected SNBT compound key")
        }
    }

    private fun parseValue() {
        when (peek()) {
            '{' -> parseCompound()
            '[' -> parseListOrArray()
            '\'', '"' -> parseQuotedString()
            null -> failAt(offset, "Expected SNBT value")
            else -> parseUnquotedScalar()
        }
    }

    private fun parseListOrArray() {
        expect('[')
        skipWhitespace()
        val arrayType = peek()
        if (arrayType in setOf('B', 'I', 'L') && source.getOrNull(offset + 1) == ';') {
            offset += 2
            parseArrayValues(arrayType!!)
            return
        }
        if (consume(']')) return
        while (true) {
            parseValue()
            skipWhitespace()
            if (consume(']')) return
            expect(',')
            skipWhitespace()
            if (peek() == ']') failAt(offset, "Expected SNBT list value")
        }
    }

    private fun parseArrayValues(arrayType: Char) {
        skipWhitespace()
        if (consume(']')) return
        while (true) {
            val valueOffset = offset
            val value = readUnquotedScalar()
            if (!isValidTypedArrayValue(value, arrayType)) {
                failAt(valueOffset, "Invalid $arrayType array value '$value'")
            }
            skipWhitespace()
            if (consume(']')) return
            expect(',')
            skipWhitespace()
            if (peek() == ']') failAt(offset, "Expected SNBT array value")
        }
    }

    private fun isValidTypedArrayValue(
        value: String,
        arrayType: Char,
    ): Boolean =
        when (arrayType) {
            'B' -> value.matches(SNBT_BYTE)
            'I' -> value.matches(SNBT_INT)
            'L' -> value.matches(SNBT_LONG)
            else -> false
        }

    private fun parseCompoundKey() {
        when (peek()) {
            '\'', '"' -> parseQuotedString()
            null -> failAt(offset, "Expected SNBT compound key")
            else -> {
                val start = offset
                while (peek()?.isAllowedInUnquotedSelectorToken() == true) offset++
                if (offset == start) failAt(start, "Expected SNBT compound key")
            }
        }
    }

    private fun parseQuotedString() {
        val quoteOffset = offset
        val quote = read()
        while (offset < source.length) {
            when (val character = read()) {
                quote -> return
                '\\' -> {
                    if (offset == source.length) {
                        failAt(quoteOffset, "Unterminated quoted SNBT string")
                    }
                    val escaped = read()
                    if (escaped != quote && escaped != '\\') {
                        failAt(offset - 2, "Invalid quoted SNBT escape")
                    }
                }
                else -> Unit
            }
        }
        failAt(quoteOffset, "Unterminated quoted SNBT string")
    }

    private fun parseUnquotedScalar() {
        readUnquotedScalar()
    }

    private fun readUnquotedScalar(): String {
        val start = offset
        while (peek()?.let { it != ',' && it != '}' && it != ']' && !it.isWhitespace() } == true) {
            if (peek()?.isAllowedInUnquotedSelectorToken() != true) {
                failAt(offset, "Invalid unquoted SNBT token")
            }
            offset++
        }
        if (offset == start) failAt(start, "Expected SNBT value")
        return source.substring(start, offset)
    }

    private fun skipWhitespace() {
        while (peek()?.isWhitespace() == true) offset++
    }

    private fun consume(expected: Char): Boolean {
        if (peek() != expected) return false
        offset++
        return true
    }

    private fun expect(expected: Char) {
        if (peek() != expected) failAt(offset, "Expected '$expected'")
        offset++
    }

    private fun read(): Char {
        if (offset == source.length) failAt(offset, "Unexpected end of SNBT")
        return source[offset++]
    }

    private fun peek(): Char? = source.getOrNull(offset)

    private fun failAt(
        localOffset: Int,
        message: String,
    ): Nothing = fail(sourceOffset + localOffset, message)
}

private val SNBT_BYTE: Regex = Regex("-?[0-9]+[bB]")
private val SNBT_INT: Regex = Regex("-?[0-9]+")
private val SNBT_LONG: Regex = Regex("-?[0-9]+[lL]")
