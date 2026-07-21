package io.github.lmliam.kotventure.core.selector

/**
 * Defines the permitted polarity combinations for a filter argument.
 */
internal enum class SelectorFilterPolicy {
    /**
     * Permits at most one positive value. Does not permit a mixture of positive values and exclusions. Permits multiple
     * exclusions.
     */
    EXCLUSIVE,

    /** Any number of positives and exclusions, in any combination. */
    REPEATABLE,
}

/**
 * Returns this keyword's filter policy, or null when it is not a filter argument.
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
 * Returns whether this filter entry is an exclusion for policy checks.
 *
 * Presence filters store their polarity in [SelectorPresence] instead of `isNegated`.
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
