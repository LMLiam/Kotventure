package io.github.lmliam.kotventure.core.selector

/**
 * Returns the keyword of this argument, or null when its argument type owns the name.
 */
internal val EntitySelectorArgument.keyword: SelectorArgumentKeyword?
    get() =
        when (this) {
            is EntitySelectorArgument.Coordinate, is EntitySelectorArgument.Range -> null
            is EntitySelectorArgument.Level -> SelectorArgumentKeyword.LEVEL
            is EntitySelectorArgument.Limit -> SelectorArgumentKeyword.LIMIT
            is EntitySelectorArgument.Sort -> SelectorArgumentKeyword.SORT
            is EntitySelectorArgument.GameMode -> SelectorArgumentKeyword.GAMEMODE
            is EntitySelectorArgument.Name -> SelectorArgumentKeyword.NAME
            is EntitySelectorArgument.Type -> SelectorArgumentKeyword.TYPE
            is EntitySelectorArgument.Tag -> SelectorArgumentKeyword.TAG
            is EntitySelectorArgument.Team -> SelectorArgumentKeyword.TEAM
            is EntitySelectorArgument.Nbt -> SelectorArgumentKeyword.NBT
            is EntitySelectorArgument.Scores -> SelectorArgumentKeyword.SCORES
            is EntitySelectorArgument.Predicate -> SelectorArgumentKeyword.PREDICATE
            is EntitySelectorArgument.Advancements -> SelectorArgumentKeyword.ADVANCEMENTS
        }

/**
 * Returns the vanilla source name of this argument, such as `limit` in `limit=1`.
 *
 * Coordinate and range types supply their argument names. Keyword arguments get their names from [keyword].
 */
internal val EntitySelectorArgument.argumentName: String
    get() =
        when (this) {
            is EntitySelectorArgument.Coordinate -> coordinate.argumentName
            is EntitySelectorArgument.Range -> argument.argumentName
            else -> checkNotNull(keyword) { "Keyword arguments always declare a keyword" }.sourceName
        }

/**
 * The argument names that can occur one time in a selector.
 */
internal val singletonSelectorArgumentNames: Set<String> =
    buildSet {
        addAll(SelectorCoordinate.entries.map { it.argumentName })
        addAll(SelectorRangeArgument.entries.map { it.argumentName })
        add(SelectorArgumentKeyword.LIMIT.sourceName)
        add(SelectorArgumentKeyword.SORT.sourceName)
        add(SelectorArgumentKeyword.LEVEL.sourceName)
        add(SelectorArgumentKeyword.SCORES.sourceName)
        add(SelectorArgumentKeyword.ADVANCEMENTS.sourceName)
    }
