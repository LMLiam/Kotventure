package io.github.lmliam.kotventure.minimessage.conversion

/**
 * Attempts to convert an SNBT compound string into a Kotlin DSL expression using `nbt { ... }`.
 *
 * Returns `null` if the SNBT contains constructs that cannot be expressed in the typed DSL
 * (e.g. generic lists), signalling the caller to fall back to `nbt("raw")`.
 */
internal fun snbtToDslExpression(snbt: String): String? {
    val parser = SnbtParser(snbt)
    val entries = parser.parseCompound() ?: return null
    if (parser.hasRemaining()) return null
    return "nbt { ${renderEntries(entries)} }"
}

private fun renderEntries(entries: List<SnbtEntry>): String =
    entries.joinToString("; ") { entry ->
        "\"${escapeKotlinString(entry.key)}\" eq ${renderDslValue(entry.value)}"
    }

private fun renderDslValue(value: SnbtParsedValue): String? =
    when (value) {
        is SnbtParsedValue.ByteVal -> "${value.value}.toByte()"
        is SnbtParsedValue.ShortVal -> "${value.value}.toShort()"
        is SnbtParsedValue.IntVal -> "${value.value}"
        is SnbtParsedValue.LongVal -> "${value.value}L"
        is SnbtParsedValue.FloatVal -> "${value.value}f"
        is SnbtParsedValue.DoubleVal -> "${value.value}"
        is SnbtParsedValue.StringVal -> "\"${escapeKotlinString(value.value)}\""
        is SnbtParsedValue.CompoundVal -> "{ ${renderEntries(value.entries)} }"
        is SnbtParsedValue.ByteArrayVal ->
            "byteArrayOf(${value.values.joinToString(", ")})"

        is SnbtParsedValue.IntArrayVal ->
            "intArrayOf(${value.values.joinToString(", ")})"

        is SnbtParsedValue.LongArrayVal ->
            "longArrayOf(${value.values.joinToString(", ") { "${it}L" }})"

        is SnbtParsedValue.Unsupported -> null
    }

internal sealed interface SnbtParsedValue {
    data class ByteVal(
        val value: Byte,
    ) : SnbtParsedValue

    data class ShortVal(
        val value: Short,
    ) : SnbtParsedValue

    data class IntVal(
        val value: Int,
    ) : SnbtParsedValue

    data class LongVal(
        val value: Long,
    ) : SnbtParsedValue

    data class FloatVal(
        val value: Float,
    ) : SnbtParsedValue

    data class DoubleVal(
        val value: Double,
    ) : SnbtParsedValue

    data class StringVal(
        val value: String,
    ) : SnbtParsedValue

    data class CompoundVal(
        val entries: List<SnbtEntry>,
    ) : SnbtParsedValue

    data class ByteArrayVal(
        val values: List<Byte>,
    ) : SnbtParsedValue

    data class IntArrayVal(
        val values: List<Int>,
    ) : SnbtParsedValue

    data class LongArrayVal(
        val values: List<Long>,
    ) : SnbtParsedValue

    data object Unsupported : SnbtParsedValue
}

internal data class SnbtEntry(
    val key: String,
    val value: SnbtParsedValue,
)

internal class SnbtParser(
    private val input: String,
) {
    private var pos = 0

    fun hasRemaining(): Boolean {
        skipWhitespace()
        return pos < input.length
    }

    fun parseCompound(): List<SnbtEntry>? {
        skipWhitespace()
        if (!consume('{')) return null
        val entries = mutableListOf<SnbtEntry>()
        skipWhitespace()
        if (peek() == '}') {
            pos++
            return entries
        }
        while (true) {
            val entry = parseEntry() ?: return null
            entries += entry
            skipWhitespace()
            if (consume('}')) break
            if (!consume(',')) return null
        }
        return entries
    }

    private fun parseEntry(): SnbtEntry? {
        skipWhitespace()
        val key = parseKey() ?: return null
        skipWhitespace()
        if (!consume(':')) return null
        skipWhitespace()
        val value = parseValue() ?: return null
        return SnbtEntry(key, value)
    }

    private fun parseKey(): String? {
        if (pos >= input.length) return null
        return if (peek() == '"') parseQuotedString() else parseUnquotedKey()
    }

    private fun parseUnquotedKey(): String? {
        val start = pos
        while (pos < input.length && isUnquotedChar(input[pos])) pos++
        if (pos == start) return null
        return input.substring(start, pos)
    }

    private fun isUnquotedChar(ch: Char): Boolean =
        ch.isLetterOrDigit() || ch == '_' || ch == '-' || ch == '.' || ch == '+'

    private fun parseValue(): SnbtParsedValue? {
        skipWhitespace()
        if (pos >= input.length) return null
        return when (peek()) {
            '{' -> {
                val entries = parseCompound() ?: return null
                SnbtParsedValue.CompoundVal(entries)
            }

            '[' -> parseArray()
            '"' -> {
                val str = parseQuotedString() ?: return null
                SnbtParsedValue.StringVal(str)
            }

            '\'' -> {
                val str = parseSingleQuotedString() ?: return null
                SnbtParsedValue.StringVal(str)
            }

            else -> parseNumberOrUnquotedString()
        }
    }

    private fun parseArray(): SnbtParsedValue? {
        if (!consume('[')) return null
        skipWhitespace()

        if (pos + 1 < input.length && input[pos + 1] == ';') {
            return when (input[pos]) {
                'B' -> parseTypedArray('B')
                'I' -> parseTypedArray('I')
                'L' -> parseTypedArray('L')
                else -> SnbtParsedValue.Unsupported
            }
        }

        return SnbtParsedValue.Unsupported
    }

    private fun parseTypedArray(type: Char): SnbtParsedValue? {
        pos += 2 // skip type char and semicolon
        skipWhitespace()
        return when (type) {
            'B' -> parseByteArray()
            'I' -> parseIntArray()
            'L' -> parseLongArray()
            else -> null
        }
    }

    private fun parseByteArray(): SnbtParsedValue? {
        val values = mutableListOf<Byte>()
        if (peek() == ']') {
            pos++
            return SnbtParsedValue.ByteArrayVal(values)
        }
        while (true) {
            skipWhitespace()
            val num = parseRawNumber() ?: return null
            val stripped = num.removeSuffix("b").removeSuffix("B")
            values += stripped.toByteOrNull() ?: return null
            skipWhitespace()
            if (consume(']')) break
            if (!consume(',')) return null
        }
        return SnbtParsedValue.ByteArrayVal(values)
    }

    private fun parseIntArray(): SnbtParsedValue? {
        val values = mutableListOf<Int>()
        if (peek() == ']') {
            pos++
            return SnbtParsedValue.IntArrayVal(values)
        }
        while (true) {
            skipWhitespace()
            val num = parseRawNumber() ?: return null
            values += num.toIntOrNull() ?: return null
            skipWhitespace()
            if (consume(']')) break
            if (!consume(',')) return null
        }
        return SnbtParsedValue.IntArrayVal(values)
    }

    private fun parseLongArray(): SnbtParsedValue? {
        val values = mutableListOf<Long>()
        if (peek() == ']') {
            pos++
            return SnbtParsedValue.LongArrayVal(values)
        }
        while (true) {
            skipWhitespace()
            val num = parseRawNumber() ?: return null
            val stripped = num.removeSuffix("l").removeSuffix("L")
            values += stripped.toLongOrNull() ?: return null
            skipWhitespace()
            if (consume(']')) break
            if (!consume(',')) return null
        }
        return SnbtParsedValue.LongArrayVal(values)
    }

    private fun parseNumberOrUnquotedString(): SnbtParsedValue {
        val start = pos
        val raw = parseRawNumber() ?: return parseUnquotedString(start)
        return classifyNumber(raw) ?: parseUnquotedString(start)
    }

    private fun parseRawNumber(): String? {
        val start = pos
        if (pos < input.length && (input[pos] == '-' || input[pos] == '+')) pos++
        if (pos >= input.length) {
            pos = start
            return null
        }
        if (!input[pos].isDigit() && input[pos] != '.') {
            pos = start
            return null
        }
        while (pos < input.length && input[pos].isDigit()) pos++
        if (pos < input.length && input[pos] == '.') {
            pos++
            while (pos < input.length && input[pos].isDigit()) pos++
        }
        if (pos < input.length && input[pos].lowercaseChar() in "eE".toList()) {
            pos++
            if (pos < input.length && (input[pos] == '+' || input[pos] == '-')) pos++
            while (pos < input.length && input[pos].isDigit()) pos++
        }
        if (pos < input.length && input[pos].lowercaseChar() in "bslfd".toList()) pos++
        if (pos == start) return null
        return input.substring(start, pos)
    }

    private fun classifyNumber(raw: String): SnbtParsedValue? {
        val lastChar = raw.last().lowercaseChar()
        return when (lastChar) {
            'b' -> {
                val n = raw.dropLast(1).toByteOrNull() ?: return null
                SnbtParsedValue.ByteVal(n)
            }

            's' -> {
                val n = raw.dropLast(1).toShortOrNull() ?: return null
                SnbtParsedValue.ShortVal(n)
            }

            'l' -> {
                val n = raw.dropLast(1).toLongOrNull() ?: return null
                SnbtParsedValue.LongVal(n)
            }

            'f' -> {
                val n = raw.dropLast(1).toFloatOrNull() ?: return null
                SnbtParsedValue.FloatVal(n)
            }

            'd' -> {
                val n = raw.dropLast(1).toDoubleOrNull() ?: return null
                SnbtParsedValue.DoubleVal(n)
            }

            else -> {
                if ('.' in raw || 'e' in raw.lowercase()) {
                    val n = raw.toDoubleOrNull() ?: return null
                    SnbtParsedValue.DoubleVal(n)
                } else {
                    val n = raw.toIntOrNull() ?: return null
                    SnbtParsedValue.IntVal(n)
                }
            }
        }
    }

    private fun parseUnquotedString(start: Int): SnbtParsedValue {
        pos = start
        while (pos < input.length && isUnquotedChar(input[pos])) pos++
        return if (pos > start) {
            SnbtParsedValue.StringVal(input.substring(start, pos))
        } else {
            SnbtParsedValue.Unsupported
        }
    }

    private fun parseQuotedString(): String? {
        if (!consume('"')) return null
        val sb = StringBuilder()
        while (pos < input.length) {
            val ch = input[pos++]
            if (ch == '"') return sb.toString()
            if (ch == '\\') {
                if (pos >= input.length) return null
                val escaped = input[pos++]
                when (escaped) {
                    '"' -> sb.append('"')
                    '\\' -> sb.append('\\')
                    'n' -> sb.append('\n')
                    't' -> sb.append('\t')
                    'r' -> sb.append('\r')
                    else -> {
                        sb.append('\\')
                        sb.append(escaped)
                    }
                }
            } else {
                sb.append(ch)
            }
        }
        return null
    }

    private fun parseSingleQuotedString(): String? {
        if (pos >= input.length || input[pos] != '\'') return null
        pos++
        val sb = StringBuilder()
        while (pos < input.length) {
            val ch = input[pos++]
            if (ch == '\'') return sb.toString()
            if (ch == '\\') {
                if (pos >= input.length) return null
                val escaped = input[pos++]
                when (escaped) {
                    '\'' -> sb.append('\'')
                    '\\' -> sb.append('\\')
                    else -> {
                        sb.append('\\')
                        sb.append(escaped)
                    }
                }
            } else {
                sb.append(ch)
            }
        }
        return null
    }

    private fun skipWhitespace() {
        while (pos < input.length && input[pos].isWhitespace()) pos++
    }

    private fun peek(): Char = if (pos < input.length) input[pos] else ' '

    private fun consume(expected: Char): Boolean {
        if (pos < input.length && input[pos] == expected) {
            pos++
            return true
        }
        return false
    }
}
