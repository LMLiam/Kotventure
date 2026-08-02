package io.github.lmliam.kotventure.core.selector

/**
 * Records selector-argument occurrences and returns the first multiplicity error.
 *
 * [EntitySelector] and the parser use this policy. The DSL builder uses [SelectorFilterGroup] because a later `!` can
 * change an entry's polarity before the block returns.
 */
internal class SelectorArgumentOccurrences {
    private val seenSingletons = mutableSetOf<String>()

    /** Stores the polarity that each exclusive filter group has established. */
    private val exclusivePolarities = mutableMapOf<String, SelectorFilterPolarity>()

    /** Records [name] and returns an error for a repeated singleton. */
    fun recordName(name: String): String? =
        if (name !in singletonSelectorArgumentNames || seenSingletons.add(name)) {
            null
        } else {
            "Selector argument '$name' may only appear once."
        }

    /**
     * Records the polarity of [argument] and returns an exclusive-filter error.
     */
    fun recordFilter(argument: EntitySelectorArgument): String? {
        val keyword = argument.keyword?.takeIf { it.filterPolicy == SelectorFilterPolicy.EXCLUSIVE } ?: return null
        val name = keyword.sourceName
        val polarity =
            if ((argument as EntitySelectorArgument.Negatable).isFilterExclusion) {
                SelectorFilterPolarity.NEGATIVE
            } else {
                SelectorFilterPolarity.POSITIVE
            }

        return when (exclusivePolarities.putIfAbsent(name, polarity)) {
            null -> null

            SelectorFilterPolarity.POSITIVE ->
                "Selector argument '$name' is already set."

            SelectorFilterPolarity.NEGATIVE ->
                if (polarity == SelectorFilterPolarity.NEGATIVE) {
                    null
                } else {
                    "Selector argument '$name' cannot combine a positive value with exclusions."
                }
        }
    }
}
