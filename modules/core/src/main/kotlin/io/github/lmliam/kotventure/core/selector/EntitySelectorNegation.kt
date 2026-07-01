package io.github.lmliam.kotventure.core.selector

/**
 * Applies negated filters to a player selector.
 *
 * @sample io.github.lmliam.kotventure.core.selector.playerSelectorNegationSample
 */
public fun PlayerEntitySelectorScope.not(init: NegatedPlayerEntitySelectorScope.() -> Unit): Unit =
    NegatedPlayerEntitySelectorAdapter(selectorState).init()

/**
 * Applies negated filters to a self selector.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selfSelectorNegationSample
 */
public fun SelfEntitySelectorScope.not(init: NegatedSelfEntitySelectorScope.() -> Unit): Unit =
    NegatedSelfEntitySelectorAdapter(selectorState).init()

private val CommonEntitySelectorScope.selectorState: EntitySelectorState
    get() = (this as EntitySelectorStateOwner).selectorState
