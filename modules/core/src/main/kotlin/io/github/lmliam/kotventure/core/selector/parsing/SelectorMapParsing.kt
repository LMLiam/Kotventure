package io.github.lmliam.kotventure.core.selector.parsing

import io.github.lmliam.kotventure.core.selector.EntitySelectorArgument
import io.github.lmliam.kotventure.core.selector.SelectorAdvancementCriterion
import io.github.lmliam.kotventure.core.selector.SelectorAdvancementProgress
import io.github.lmliam.kotventure.core.selector.SelectorAdvancementRequirement
import io.github.lmliam.kotventure.core.selector.SelectorScoreRequirement

internal fun SelectorReader.readScoresArgument(): EntitySelectorArgument.Scores =
    EntitySelectorArgument.Scores(
        readSelectorMap("score objective") { objective, objectiveOffset ->
            validateUnquotedToken(objective, objectiveOffset)
            SelectorScoreRequirement(objective, readIntRange(objective, nonNegative = false))
        },
    )

internal fun SelectorReader.readAdvancementsArgument(): EntitySelectorArgument.Advancements =
    EntitySelectorArgument.Advancements(
        readSelectorMap("advancement") { advancement, advancementOffset ->
            SelectorAdvancementRequirement(
                advancement = parseSelectorKey(advancement, advancementOffset),
                progress = readAdvancementProgress(),
            )
        },
    )

private fun SelectorReader.readAdvancementProgress(): SelectorAdvancementProgress =
    when (peek()) {
        '{' -> SelectorAdvancementProgress.Criteria(readAdvancementCriteria())
        else -> SelectorAdvancementProgress.Completion(readSelectorBoolean())
    }

private fun SelectorReader.readAdvancementCriteria(): List<SelectorAdvancementCriterion> =
    readSelectorMap("advancement criterion") { criterion, criterionOffset ->
        validateUnquotedToken(criterion, criterionOffset)
        SelectorAdvancementCriterion(criterion, readSelectorBoolean())
    }

private fun <T> SelectorReader.readSelectorMap(
    entryName: String,
    readEntry: SelectorReader.(key: String, keyOffset: Int) -> T,
): List<T> {
    expect('{')
    if (consume('}')) return emptyList()

    return buildList {
        while (true) {
            val keyOffset = offset
            val key = readWhile { it != '=' && it != ',' && it != '}' }
            if (key.isEmpty()) failAt(keyOffset, "Expected $entryName")
            expect('=')
            add(readEntry(key, keyOffset))

            when {
                consume('}') -> break
                consume(',') -> if (peek() == '}') fail("Expected $entryName")
                else -> fail("Expected ',' or '}'")
            }
        }
    }
}
