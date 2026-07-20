package io.github.lmliam.kotventure.core.selector

/**
 * The entries of one selector argument, in call order.
 *
 * A statement's polarity is final when the statement completes. The `!` operator applies before the next statement.
 * Thus, an exclusive group can reject an addition after a positive entry at that call site. One error is visible only
 * later: exclusions followed by a positive entry that stays positive. [validate] finds this error when the selector
 * block ends.
 *
 * [SelectorArgumentKeyword.filterPolicy] supplies the policy for [keyword]. Thus, the builder stays consistent with
 * model and parser validation.
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
     * Adds [value] with a fixed [polarity] and does not return a negatable value. Presence filters such as `tag(any)`
     * and `team(none)` contain their polarity in the value. Thus, `!` cannot change it again.
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
