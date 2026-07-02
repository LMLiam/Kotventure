package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.ParsedAdvancementCriterion
import io.github.lmliam.kotventure.core.selector.ParsedAdvancementProgress
import io.github.lmliam.kotventure.core.selector.ParsedSelectorAdvancement
import io.github.lmliam.kotventure.core.selector.ParsedSelectorScore

internal fun SelectorReader.readScoresArgument(): EntitySelectorArgument.Scores =
    EntitySelectorArgument.Scores(
        readSelectorMap("score objective") { objective, objectiveOffset ->
            validateUnquotedToken(objective, objectiveOffset)
            ParsedSelectorScore(objective, readIntRange(objective, nonNegative = false))
        },
    )

internal fun SelectorReader.readAdvancementsArgument(): EntitySelectorArgument.Advancements =
    EntitySelectorArgument.Advancements(
        readSelectorMap("advancement") { advancement, advancementOffset ->
            ParsedSelectorAdvancement(
                advancement = parseSelectorKey(advancement, advancementOffset),
                progress = readAdvancementProgress(),
            )
        },
    )

private fun SelectorReader.readAdvancementProgress(): ParsedAdvancementProgress =
    if (peek() == '{') {
        ParsedAdvancementProgress.Criteria(readAdvancementCriteria())
    } else {
        ParsedAdvancementProgress.Completion(readSelectorBoolean())
    }

private fun SelectorReader.readAdvancementCriteria(): List<ParsedAdvancementCriterion> =
    readSelectorMap("advancement criterion") { criterion, criterionOffset ->
        validateUnquotedToken(criterion, criterionOffset)
        ParsedAdvancementCriterion(criterion, readSelectorBoolean())
    }

private fun <T> SelectorReader.readSelectorMap(
    entryName: String,
    readEntry: SelectorReader.(key: String, keyOffset: Int) -> T,
): List<T> {
    expect('{')
    if (consume('}')) return emptyList()
    val entries = mutableListOf<T>()
    while (true) {
        val keyOffset = offset
        val key = readWhile { it != '=' && it != ',' && it != '}' }
        if (key.isEmpty()) failAt(keyOffset, "Expected $entryName")
        expect('=')
        entries += readEntry(key, keyOffset)
        when {
            consume('}') -> return entries
            consume(',') -> if (peek() == '}') fail("Expected $entryName")
            else -> fail("Expected ',' or '}'")
        }
    }
}
