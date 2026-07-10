package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.selector.parsing.SelectorReader
import io.github.lmliam.kotventure.core.selector.parsing.readArgumentValue

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
        val seenSingletons = mutableSetOf<String>()
        val filterPolarity = mutableMapOf<String, ParseFilterPolarityState>()
        while (true) {
            add(readSelectorArgument(head, seenSingletons, filterPolarity))
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
    seenSingletons: MutableSet<String>,
    filterPolarity: MutableMap<String, ParseFilterPolarityState>,
): EntitySelectorArgument {
    val nameOffset = offset
    val name = readWhile { it != '=' && it != ',' && it != ']' }
    if (name.isEmpty()) failAt(nameOffset, "Expected selector argument")
    if (name in singletonSelectorArgumentNames && !seenSingletons.add(name)) {
        failAt(
            nameOffset,
            "Selector argument '$name' may only appear once (vanilla syntax allows a single occurrence).",
        )
    }
    expect('=', "Expected '=' after selector argument '$name'")
    val argument = readArgumentValue(head, name, nameOffset)
    enforceFilterGroupPolicy(argument, name, nameOffset, filterPolarity)
    return argument
}

/**
 * Rejects exclusive filter-group violations at the offending argument name offset, using the same
 * policy and messages as [EntitySelector] / [SelectorFilterGroup].
 */
private fun SelectorReader.enforceFilterGroupPolicy(
    argument: EntitySelectorArgument,
    name: String,
    nameOffset: Int,
    filterPolarity: MutableMap<String, ParseFilterPolarityState>,
) {
    val keyword = argument.keyword ?: return
    val policy = keyword.filterPolicy ?: return
    val state = filterPolarity.getOrPut(name) { ParseFilterPolarityState() }
    val isExclusion = (argument as EntitySelectorArgument.Negatable).isFilterExclusion
    val violation = policy.violationFor(name, state.hasPositive, state.hasNegative, isExclusion)
    if (violation != null) failAt(nameOffset, violation)
    if (isExclusion) state.hasNegative = true else state.hasPositive = true
}

private class ParseFilterPolarityState {
    var hasPositive: Boolean = false
    var hasNegative: Boolean = false
}
