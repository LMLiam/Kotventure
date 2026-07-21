package io.github.lmliam.kotventure.core.selector

/**
 * Collects one filter argument in call order.
 *
 * The caller can negate an entry after it is added. [validate] therefore checks the final polarity when the selector
 * block returns.
 */
internal class SelectorFilterGroup<T>(
    keyword: SelectorArgumentKeyword,
) {
    val argument: String = keyword.sourceName
    private val policy: SelectorFilterPolicy =
        requireNotNull(keyword.filterPolicy) {
            "Selector argument '${keyword.sourceName}' is not a filter group"
        }

    val entries: List<SelectorFilterEntry<T>>
        field = mutableListOf()

    fun add(
        owner: EntitySelectorBuilder,
        value: T,
    ): SelectorFilterExpression = addEntry(owner, value, SelectorFilterPolarity.POSITIVE)

    /**
     * Adds [value] with a fixed [polarity]. Presence filters store their polarity in the value and cannot be negated
     * again.
     */
    fun addFixed(
        owner: EntitySelectorBuilder,
        value: T,
        polarity: SelectorFilterPolarity,
    ) {
        addEntry(owner, value, polarity)
    }

    fun validate() {
        if (policy != SelectorFilterPolicy.EXCLUSIVE) return

        val hasPositive = entries.any { it.polarity == SelectorFilterPolarity.POSITIVE }
        val hasNegative = entries.any { it.polarity == SelectorFilterPolarity.NEGATIVE }
        check(!(hasPositive && hasNegative)) {
            "Selector argument '$argument' cannot combine a positive value with exclusions."
        }
    }

    private fun addEntry(
        owner: EntitySelectorBuilder,
        value: T,
        polarity: SelectorFilterPolarity,
    ): SelectorFilterEntry<T> {
        if (policy == SelectorFilterPolicy.EXCLUSIVE) {
            check(entries.none { it.polarity == SelectorFilterPolarity.POSITIVE }) {
                "Selector argument '$argument' is already set."
            }
        }
        return SelectorFilterEntry(owner, argument, value, polarity).also {
            entries += it
        }
    }
}
