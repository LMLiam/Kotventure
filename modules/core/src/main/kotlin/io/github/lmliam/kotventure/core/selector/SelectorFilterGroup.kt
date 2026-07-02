package io.github.lmliam.kotventure.core.selector

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

        val positiveCount = entries.count { it.polarity == SelectorFilterPolarity.POSITIVE }
        check(positiveCount <= 1) {
            "Selector argument '$argument' is already set; vanilla syntax allows it only once."
        }
        check(positiveCount == 0 || entries.none { it.polarity == SelectorFilterPolarity.NEGATIVE }) {
            "Selector argument '$argument' cannot combine a positive value with exclusions."
        }
    }

    private fun addEntry(
        owner: EntitySelectorBuilder,
        value: T,
        polarity: SelectorFilterPolarity,
    ): SelectorFilterEntry<T> =
        SelectorFilterEntry(owner, argument, value, polarity).also {
            entries += it
        }
}
