package io.github.lmliam.kotventure.core.selector

/**
 * A transient selector-filter expression that may be negated with `!` inside its creating selector block.
 *
 * Expressions cannot be constructed directly or reused across selector blocks.
 *
 * @sample io.github.lmliam.kotventure.core.selector.negatedCommonArgumentsSample
 */
public sealed interface SelectorFilterExpression
