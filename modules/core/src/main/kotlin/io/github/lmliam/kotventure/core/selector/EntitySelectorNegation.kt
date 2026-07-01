package io.github.lmliam.kotventure.core.selector

/**
 * Applies negated filters to a player selector.
 *
 * @sample io.github.lmliam.kotventure.core.selector.playerSelectorNegationSample
 */
public fun PlayerEntitySelectorScope.not(init: NegatedCommonEntitySelectorScope.() -> Unit): Unit =
    NegatedCommonEntitySelectorAdapter(selectorState).init()

/**
 * Applies negated filters to a self selector.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selfSelectorNegationSample
 */
public fun SelfEntitySelectorScope.not(init: NegatedEntitySelectorScope.() -> Unit): Unit =
    NegatedEntitySelectorAdapter(selectorState).init()

private val CommonEntitySelectorScope.selectorState: EntitySelectorState
    get() = (this as EntitySelectorStateOwner).selectorState
