package io.github.lmliam.kotventure.core.selector

/**
 * A transient selector-filter expression that `!` can negate in its selector block.
 *
 * You cannot construct an expression directly or reuse it in a different selector block. You can negate it only one
 * time and only before its creating selector block returns.
 *
 * @sample io.github.lmliam.kotventure.core.selector.negatedCommonArgumentsSample
 */
public sealed interface SelectorFilterExpression
