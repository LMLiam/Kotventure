package io.github.lmliam.kotventure.core.selector

/**
 * Records selector-argument occurrences and returns the first multiplicity error.
 *
 * [EntitySelector] and the parser use this policy. The DSL builder uses [SelectorFilterGroup] because a later `!` can
 * change an entry's polarity before the block returns.
 */
internal class SelectorArgumentOccurrences {
    private val singletons = mutableSetOf<String>()

    /** Stores the polarity that each exclusive filter group has established. */
    private val exclusivePolarities = mutableMapOf<String, SelectorFilterPolarity>()

    /** Records [name] and returns an error for a repeated singleton. */
    fun recordName(name: String): String? =
        when {
            name !in singletonSelectorArgumentNames -> null
            singletons.add(name) -> null
            else -> "Selector argument '$name' may only appear once."
        }

    /**
     * Records the polarity of [argument] and returns an exclusive-filter error.
     */
    fun recordFilter(argument: EntitySelectorArgument): String? {
        val keyword = argument.keyword ?: return null
        if (keyword.filterPolicy != SelectorFilterPolicy.EXCLUSIVE) return null

        val name = keyword.sourceName
        val isExclusion = (argument as EntitySelectorArgument.Negatable).isFilterExclusion

        return when (exclusivePolarities[name]) {
            SelectorFilterPolarity.POSITIVE ->
                "Selector argument '$name' is already set."

            SelectorFilterPolarity.NEGATIVE ->
                if (isExclusion) null else "Selector argument '$name' cannot combine a positive value with exclusions."

            null -> {
                exclusivePolarities[name] =
                    if (isExclusion) SelectorFilterPolarity.NEGATIVE else SelectorFilterPolarity.POSITIVE
                null
            }
        }
    }
}
