package io.github.lmliam.kotventure.core.selector

/**
 * The keyword of this argument, or `null` for coordinates and floating-point ranges (whose names
 * are owned by [SelectorCoordinate] and [SelectorRangeArgument]).
 *
 * Exhaustive: a new argument subtype cannot be added without updating this mapping. This ensures
 * that rendering and parsing stay in sync.
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
 * The vanilla selector-source name of this argument, such as `limit` in `limit=1`.
 *
 * Coordinates and ranges are named by their own argument types; keyword arguments resolve their
 * names from the [keyword] property.
 */
internal val EntitySelectorArgument.argumentName: String
    get() =
        when (this) {
            is EntitySelectorArgument.Coordinate -> coordinate.argumentName
            is EntitySelectorArgument.Range -> argument.argumentName
            else -> checkNotNull(keyword) { "Keyword arguments always declare a keyword" }.sourceName
        }
