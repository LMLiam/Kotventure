package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.selector.parsing.SelectorReader
import io.github.lmliam.kotventure.core.selector.parsing.readArgumentValue
import io.github.lmliam.kotventure.core.selector.readSelectorArgument

/**
 * Parses one complete Java Edition entity selector.
 *
 * The parser accepts the six heads in [EntitySelectorHead] and the typed arguments that their scopes expose. Selector
 * syntax does not permit extra whitespace. Quoted strings and SNBT values can contain the whitespace that their own
 * syntax permits. The returned selector keeps source argument order. Its [EntitySelector.asString] output uses
 * canonical number, quoting, and empty-argument forms.
 *
 * @throws EntitySelectorParseException when [source] is invalid, unsupported, or has trailing content. The exception
 * identifies the zero-based source offset at which parsing failed.
 * @sample io.github.lmliam.kotventure.core.selector.parseSelectorSample
 */
public fun parseSelector(source: String): EntitySelector = SelectorReader(source).readEntitySelector()

private fun SelectorReader.readEntitySelector(): EntitySelector {
    val head = readSelectorHead()
    if (isAtEnd()) return EntitySelector(head, emptyList())

    expect('[', "Expected '[' or the end of the selector")
    val arguments = readSelectorArguments(head)
    if (!isAtEnd()) fail("Unexpected trailing selector content")
    return EntitySelector(head, arguments)
}

private fun SelectorReader.readSelectorHead(): EntitySelectorHead {
    expect('@', "Expected '@' to begin an entity selector")
    val tokenOffset = offset
    val token = "@${readWhile { it != '[' }}"
    return EntitySelectorHead.entries.firstOrNull { it.token == token }
        ?: failAt(tokenOffset, "Unsupported selector head")
}

private fun SelectorReader.readSelectorArguments(head: EntitySelectorHead): List<EntitySelectorArgument> {
    if (consume(']')) return emptyList()

    return buildList {
        val occurrences = SelectorArgumentOccurrences()
        while (true) {
            add(readSelectorArgument(head, occurrences))
            when {
                consume(']') -> return@buildList
                consume(',') -> if (peek() == ']') fail("Expected selector argument")
                else -> fail("Expected ']' or another selector argument")
            }
        }
    }
}

private fun SelectorReader.readSelectorArgument(
    head: EntitySelectorHead,
    occurrences: SelectorArgumentOccurrences,
): EntitySelectorArgument {
    val nameOffset = offset
    val name = readWhile { it !in "=,]" }
    if (name.isEmpty()) failAt(nameOffset, "Expected selector argument")
    occurrences.recordName(name)?.let { failAt(nameOffset, it) }
    expect('=', "Expected '=' after selector argument '$name'")
    val argument = readArgumentValue(head, name, nameOffset)
    occurrences.recordFilter(argument)?.let { failAt(nameOffset, it) }
    return argument
}
