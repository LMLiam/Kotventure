package io.github.lmliam.kotventure.core.selector

/**
 * Multiplicity policy for a filter-group selector argument (`name`, `type`, `tag`, …).
 *
 * Single source of truth for the DSL builder ([SelectorFilterGroup]), the immutable model
 * ([EntitySelector]), and the parse path — so the three cannot drift.
 */
internal enum class SelectorFilterPolicy {
    /**
     * At most one positive value; positives cannot be mixed with exclusions.
     * Multiple exclusions alone are allowed.
     */
    EXCLUSIVE,

    /** Any number of positives and exclusions, in any combination. */
    REPEATABLE,
}

/**
 * Filter-group multiplicity for this keyword, or `null` when the keyword is a singleton (or not a
 * filter group).
 */
internal val SelectorArgumentKeyword.filterPolicy: SelectorFilterPolicy?
    get() =
        when (this) {
            SelectorArgumentKeyword.TYPE,
            SelectorArgumentKeyword.NAME,
            SelectorArgumentKeyword.GAMEMODE,
            SelectorArgumentKeyword.TEAM,
                -> SelectorFilterPolicy.EXCLUSIVE

            SelectorArgumentKeyword.TAG,
            SelectorArgumentKeyword.NBT,
            SelectorArgumentKeyword.PREDICATE,
                -> SelectorFilterPolicy.REPEATABLE

            SelectorArgumentKeyword.LEVEL,
            SelectorArgumentKeyword.LIMIT,
            SelectorArgumentKeyword.SORT,
            SelectorArgumentKeyword.SCORES,
            SelectorArgumentKeyword.ADVANCEMENTS,
                -> null
        }

/**
 * Whether this filter-group entry is an exclusion for exclusive-policy purposes.
 *
 * Named/`!value` filters use [EntitySelectorArgument.Negatable.isNegated]. Presence filters encode
 * polarity in the presence value itself (`team=!` / `tag=!` → [SelectorPresence.ANY]), so
 * [SelectorStringCondition.Presence.isNegated] stays `false` and must not be used here.
 */
internal val EntitySelectorArgument.Negatable.isFilterExclusion: Boolean
    get() =
        when (this) {
            is EntitySelectorArgument.Tag -> condition.isFilterExclusion
            is EntitySelectorArgument.Team -> condition.isFilterExclusion
            else -> isNegated
        }

private val SelectorStringCondition.isFilterExclusion: Boolean
    get() =
        when (this) {
            is SelectorStringCondition.Named -> isNegated
            is SelectorStringCondition.Presence -> value == SelectorPresence.ANY
        }
