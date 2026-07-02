package io.github.lmliam.kotventure.core.selector

/**
 * The entries of one selector argument, in call order.
 *
 * A statement's polarity is final once it completes (`!` applies before the next statement runs),
 * so an exclusive group can reject any addition after a positive entry at the offending call site.
 * The one violation only visible later — exclusions followed by a positive that never gets
 * negated — is caught by [validate] when the selector block ends.
 */
internal class SelectorFilterGroup<T>(
    private val argument: String,
    private val policy: SelectorFilterPolicy,
) {
    val entries: List<SelectorFilterEntry<T>>
        field = mutableListOf()

    fun add(
        owner: EntitySelectorBuilder,
        value: T,
    ): SelectorFilterExpression = addEntry(owner, value, SelectorFilterPolarity.POSITIVE)

    fun addFixed(
        owner: EntitySelectorBuilder,
        value: T,
        polarity: SelectorFilterPolarity,
    ) {
        addEntry(owner, value, polarity)
    }

    fun validate() {
        if (policy == SelectorFilterPolicy.REPEATABLE) return

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
                "Selector argument '$argument' is already set; vanilla syntax allows one positive value."
            }
        }
        return SelectorFilterEntry(owner, argument, value, polarity).also {
            entries += it
        }
    }
}
