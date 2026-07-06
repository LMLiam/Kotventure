package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.selector.parsing.SelectorReader
import io.github.lmliam.kotventure.core.selector.parsing.readArgumentValue
import io.github.lmliam.kotventure.core.selector.readSelectorArgument
import io.github.lmliam.kotventure.core.selector.readSelectorArguments

/**
 * Parses Java Edition entity-selector source into an [EntitySelector].
 *
 * @throws EntitySelectorParseException if [source] is not valid selector syntax
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
        while (true) {
            add(readSelectorArgument(head))
            when {
                consume(']') -> return@buildList
                consume(',') -> if (peek() == ']') fail("Expected selector argument")
                else -> fail("Expected ']' or another selector argument")
            }
        }
    }
}

private fun SelectorReader.readSelectorArgument(head: EntitySelectorHead): EntitySelectorArgument {
    val nameOffset = offset
    val name = readWhile { it != '=' && it != ',' && it != ']' }
    if (name.isEmpty()) failAt(nameOffset, "Expected selector argument")
    expect('=', "Expected '=' after selector argument '$name'")
    return readArgumentValue(head, name, nameOffset)
}
