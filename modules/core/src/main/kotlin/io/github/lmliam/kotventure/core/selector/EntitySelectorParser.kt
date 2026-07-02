package io.github.lmliam.kotventure.core.selector

/**
 * Parses a Java Edition entity selector into an immutable typed model.
 *
 * Parsing is opt-in: [entitySelector] remains the lossless escape hatch for syntax this parser
 * does not support. Unsupported arguments return [EntitySelectorParseResult.Failure] rather than
 * being discarded.
 *
 * @sample io.github.lmliam.kotventure.core.selector.parsedEntitySelectorSample
 */
public fun parseEntitySelector(source: String): EntitySelectorParseResult =
    try {
        EntitySelectorParseResult.Success(EntitySelectorSourceParser(source).parse())
    } catch (failure: SelectorParseFailure) {
        EntitySelectorParseResult.Failure(failure.error)
    }

private class EntitySelectorSourceParser(
    private val source: String,
) {
    private var offset: Int = 0

    fun parse(): ParsedEntitySelector {
        expect('@', "Expected '@' to begin an entity selector")
        val headOffset = offset
        val head =
            when (read()) {
                'p' -> EntitySelectorHead.NEAREST_PLAYER
                'a' -> EntitySelectorHead.ALL_PLAYERS
                'r' -> EntitySelectorHead.RANDOM_PLAYER
                's' -> EntitySelectorHead.SELF
                'e' -> EntitySelectorHead.ENTITIES
                'n' -> EntitySelectorHead.NEAREST_ENTITY
                else -> fail(headOffset, "Unsupported selector head")
            }
        if (offset == source.length) {
            return ParsedEntitySelector(head, emptyList())
        }
        expect('[', "Expected '[' or the end of the selector")
        val arguments = parseArguments(head)
        if (offset != source.length) {
            fail(offset, "Unexpected trailing selector content")
        }
        return ParsedEntitySelector(head, arguments, hasExplicitArgumentList = true)
    }

    private fun parseArguments(head: EntitySelectorHead): List<EntitySelectorArgument> {
        val arguments = mutableListOf<EntitySelectorArgument>()
        if (peek() == ']') {
            offset++
            return emptyList()
        }
        while (true) {
            arguments += parseArgument(head)
            when (peek()) {
                ',' -> {
                    offset++
                    if (peek() == ']') {
                        fail(offset, "Expected selector argument")
                    }
                }
                ']' -> {
                    offset++
                    return arguments.immutableSnapshot()
                }
                else -> fail(offset, "Expected ']' or another selector argument")
            }
        }
    }

    private fun parseArgument(head: EntitySelectorHead): EntitySelectorArgument {
        val nameOffset = offset
        val name = readArgumentName()
        if (name.isEmpty()) {
            fail(nameOffset, "Expected selector argument")
        }
        expect('=', "Expected '=' after selector argument '$name'")
        val valueOffset = offset
        val value = readArgumentValue()
        return parseSelectorArgument(head, name, value, nameOffset, valueOffset)
    }

    private fun readArgumentName(): String {
        val start = offset
        while (peek()?.let { it != '=' && it != ',' && it != ']' } == true) {
            offset++
        }
        return source.substring(start, offset)
    }

    private fun readArgumentValue(): String {
        val start = offset
        val delimiters = ArrayDeque<Char>()
        var quote: Char? = null
        var quoteOffset = -1
        while (offset < source.length) {
            val character = source[offset]
            if (quote != null) {
                when {
                    character == '\\' -> {
                        offset++
                        if (offset == source.length) {
                            fail(quoteOffset, "Unterminated quoted string")
                        }
                    }
                    character == quote -> quote = null
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
                        if (character == ']') break
                        fail(offset, "Unexpected '$character'")
                    } else if (delimiters.removeLast() != character) {
                        fail(offset, "Mismatched '$character'")
                    }
                ',' -> if (delimiters.isEmpty()) break
            }
            offset++
        }
        if (quote != null) {
            fail(quoteOffset, "Unterminated quoted string")
        }
        if (delimiters.isNotEmpty()) {
            fail(offset, "Expected '${delimiters.last()}'")
        }
        return source.substring(start, offset)
    }

    private fun expect(
        expected: Char,
        message: String,
    ) {
        if (peek() != expected) fail(offset, message)
        offset++
    }

    private fun read(): Char {
        if (offset == source.length) fail(offset, "Expected selector head")
        return source[offset++]
    }

    private fun peek(): Char? = source.getOrNull(offset)
}

internal class SelectorParseFailure(
    offset: Int,
    message: String,
) : RuntimeException(message) {
    val error: EntitySelectorParseError = EntitySelectorParseError(offset, message)
}

internal fun fail(
    offset: Int,
    message: String,
): Nothing = throw SelectorParseFailure(offset, message)
