package io.github.lmliam.kotventure.core.selector

internal class SelectorFilterEntry<T>(
    val owner: EntitySelectorBuilder,
    val argument: String,
    val value: T,
    initialPolarity: SelectorFilterPolarity,
) : SelectorFilterExpression {
    var polarity: SelectorFilterPolarity = initialPolarity
        private set

    fun negate(requester: EntitySelectorBuilder) {
        check(owner === requester) { "Selector filter expressions cannot be reused across selector blocks." }
        check(polarity == SelectorFilterPolarity.POSITIVE) {
            "Selector filter expression for '$argument' is already negated."
        }
        polarity = SelectorFilterPolarity.NEGATIVE
    }
}
