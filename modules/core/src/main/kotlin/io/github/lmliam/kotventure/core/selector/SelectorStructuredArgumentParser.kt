package io.github.lmliam.kotventure.core.selector

internal fun parseNbtArgument(
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Nbt {
    val negated = value.startsWith('!')
    val snbt = value.removeSelectorNegation()
    val snbtOffset = valueOffset + if (negated) 1 else 0
    SelectorSnbtValidator(snbt, snbtOffset).validateCompound()
    return EntitySelectorArgument.Nbt(snbt, negated)
}

internal fun parseScoresArgument(
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Scores {
    val scores =
        SelectorMapParser(value, valueOffset, "score objective").parse {
                key,
                keyOffset,
                entry,
                entryOffset,
            ->
            validateSelectorUnquotedToken(key, keyOffset)
            ParsedSelectorScore(key, parseSelectorLevelRange(entry, entryOffset, nonNegative = false))
        }
    return EntitySelectorArgument.Scores(scores)
}

internal fun parseAdvancementsArgument(
    value: String,
    valueOffset: Int,
): EntitySelectorArgument.Advancements {
    val advancements =
        SelectorMapParser(value, valueOffset, "advancement").parse { key, keyOffset, entry, entryOffset ->
            val progress =
                if (entry.startsWith('{')) {
                    ParsedAdvancementProgress.Criteria(parseAdvancementCriteria(entry, entryOffset))
                } else {
                    ParsedAdvancementProgress.Completion(parseSelectorBoolean(entry, entryOffset))
                }
            ParsedSelectorAdvancement(parseSelectorKey(key, keyOffset), progress)
        }
    return EntitySelectorArgument.Advancements(advancements)
}

private fun parseAdvancementCriteria(
    value: String,
    valueOffset: Int,
): List<ParsedAdvancementCriterion> =
    SelectorMapParser(value, valueOffset, "advancement criterion").parse {
            key,
            keyOffset,
            entry,
            entryOffset,
        ->
        validateSelectorUnquotedToken(key, keyOffset)
        ParsedAdvancementCriterion(key, parseSelectorBoolean(entry, entryOffset))
    }
