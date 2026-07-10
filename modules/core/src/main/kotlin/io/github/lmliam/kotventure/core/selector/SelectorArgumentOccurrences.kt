package io.github.lmliam.kotventure.core.selector

/**
 * Records selector-argument occurrences and reports the first multiplicity violation:
singleton
 * arguments ([singletonSelectorArgumentNames]) may appear only once, and an exclusive
filter group
 * allows one positive value that cannot be mixed with exclusions.
 *
 * This is the single validation behind [EntitySelector]'s constructor and the parser,
which
 * surfaces the returned message at the offending argument's source offset.
[SelectorFilterGroup]
 * cannot share it: DSL polarity is only final when the block ends (`!` negates an entry
after it
 * is added), so the builder validates in two phases instead.
 */
internal class SelectorArgumentOccurrences {
    private val singletons = mutableSetOf<String>()

    /** Per-name polarity of an exclusive filter group: [SelectorFilterPolarity.NEGATIVE] once any exclusion is recorded. */
    private val exclusivePolarities = mutableMapOf<String, SelectorFilterPolarity>()

    /** Records one occurrence of [name], returning the violation for a repeated singleton. */
    fun recordName(name: String): String? =
        when {
            name !in singletonSelectorArgumentNames -> null
            singletons.add(name) -> null
            else -> "Selector argument '$name' may only appear once."
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
