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

/**
 * Vanilla argument names that may appear at most once in a selector (coordinates, ranges, and
 * non-filter-group keywords).
 *
 * Single source of truth for singleton policy: [EntitySelectorArgument.singletonKey] and the
 * parser both consult this set so model validation and parse-time rejection cannot diverge.
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

/**
 * Key used for singleton uniqueness (DSL and parse). Filter-group arguments are multi-valued under
 * [SelectorFilterPolicy] and return `null`.
 *
 * Derived from [singletonSelectorArgumentNames] via [argumentName] — do not re-list singletons here.
 */
internal val EntitySelectorArgument.singletonKey: String?
    get() = argumentName.takeIf { it in singletonSelectorArgumentNames }

/**
 * Fail-fast message when a singleton selector argument appears more than once.
 *
 * Shared by the DSL builder, model constructor, and parser so wording stays identical.
 */
internal fun selectorSingletonAlreadySetMessage(argument: String): String =
    "Selector argument '$argument' may only appear once (vanilla syntax allows a single occurrence)."
