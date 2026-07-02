package io.github.lmliam.kotventure.core.selector

/**
 * Validates and parses Java Edition entity-selector source into an immutable [EntitySelector].
 *
 * Parsing is grammar-strict: the six selector heads and every argument understood by the selector
 * DSL parse into typed arguments, and any other syntax throws
 * [EntitySelectorParseException] instead of being silently normalized. Semantic rules the game
 * applies on top of the grammar — such as rejecting duplicate single-use arguments or tolerating
 * whitespace between arguments — are deliberately out of scope until the vanilla-conformance
 * suite pins them.
 *
 * @throws EntitySelectorParseException if [source] is not a parseable entity selector
 * @sample io.github.lmliam.kotventure.core.selector.parsedEntitySelectorSample
 */
public fun entitySelector(source: String): EntitySelector {
    val reader = SelectorReader(source)
    val head = reader.readSelectorHead()
    if (reader.isAtEnd()) return EntitySelector(head, emptyList())
    reader.expect('[', "Expected '[' or the end of the selector")
    val arguments = reader.readSelectorArguments(head)
    if (!reader.isAtEnd()) reader.fail("Unexpected trailing selector content")
    return EntitySelector(head, arguments, hasExplicitArgumentList = true)
}

private fun SelectorReader.readSelectorHead(): EntitySelectorHead {
    expect('@', "Expected '@' to begin an entity selector")
    val tokenOffset = offset
    val character = peek() ?: fail("Expected selector head")
    val head =
        EntitySelectorHead.entries.firstOrNull { it.token == "@$character" }
            ?: failAt(tokenOffset, "Unsupported selector head")
    skip()
    return head
}

private fun SelectorReader.readSelectorArguments(head: EntitySelectorHead): List<EntitySelectorArgument> {
    if (consume(']')) return emptyList()
    val arguments = mutableListOf<EntitySelectorArgument>()
    while (true) {
        arguments += readSelectorArgument(head)
        when {
            consume(']') -> return arguments
            consume(',') -> if (peek() == ']') fail("Expected selector argument")
            else -> fail("Expected ']' or another selector argument")
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
