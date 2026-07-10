package io.github.lmliam.kotventure.core.selector

/**
 * Records selector-argument occurrences and reports the first multiplicity violation: singleton
 * arguments ([singletonSelectorArgumentNames]) may appear only once, and an exclusive filter group
 * allows one positive value that cannot be mixed with exclusions.
 *
 * The single validation behind [EntitySelector]'s constructor and the parser (which surfaces the
 * returned message at the offending argument's source offset). [SelectorFilterGroup] cannot share
 * it: DSL polarity is only final when the block ends (`!` negates an entry after it is added), so
 * the builder validates in two phases instead.
 */
internal class SelectorArgumentOccurrences {
    private val singletons = mutableSetOf<String>()
    private val positives = mutableSetOf<String>()
    private val negatives = mutableSetOf<String>()

    /** Records one occurrence of [name], returning the violation for a repeated singleton. */
    fun recordName(name: String): String? {
        if (name !in singletonSelectorArgumentNames || singletons.add(name)) return null
        return "Selector argument '$name' may only appear once (vanilla syntax allows a single occurrence)."
    }

    /**
     * Records [argument]'s filter polarity, returning the violation for an exclusive-policy
     * breach. Singleton and [SelectorFilterPolicy.REPEATABLE] arguments are never violations.
     */
    fun recordFilter(argument: EntitySelectorArgument): String? {
        val keyword = argument.keyword ?: return null
        if (keyword.filterPolicy != SelectorFilterPolicy.EXCLUSIVE) return null

        val name = keyword.sourceName
        val isExclusion = (argument as EntitySelectorArgument.Negatable).isFilterExclusion
        return when {
            name in positives ->
                "Selector argument '$name' is already set; vanilla syntax allows one positive value."
            !isExclusion && name in negatives ->
                "Selector argument '$name' cannot combine a positive value with exclusions."
            else -> {
                if (isExclusion) negatives += name else positives += name
                null
            }
        }
    }
}
