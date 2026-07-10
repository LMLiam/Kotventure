package io.github.lmliam.kotventure.core.selector

/**
 * The entries of one selector argument, in call order.
 *
 * A statement's polarity is final once it completes (`!` applies before the next statement runs),
 * so an exclusive group can reject any addition after a positive entry at the offending call site.
 * The one violation only visible later — exclusions followed by a positive that never gets
 * negated — is caught by [validate] when the selector block ends.
 *
 * Policy is resolved from [keyword] via [SelectorArgumentKeyword.filterPolicy] so the builder
 * cannot drift from model/parse validation.
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
            exclusivePolarityMixMessage(argument)
        }
    }

    private fun addEntry(
        owner: EntitySelectorBuilder,
        value: T,
        polarity: SelectorFilterPolarity,
    ): SelectorFilterEntry<T> {
        if (policy == SelectorFilterPolicy.EXCLUSIVE) {
            check(entries.none { it.polarity == SelectorFilterPolarity.POSITIVE }) {
                exclusivePositiveAlreadySetMessage(argument)
            }
        }
        return SelectorFilterEntry(owner, argument, value, polarity).also {
            entries += it
        }
    }
}
