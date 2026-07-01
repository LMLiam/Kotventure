package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Complete scope for selectors that support every typed entity-selector argument.
 *
 * @sample io.github.lmliam.kotventure.core.selector.entitySelectorScopeSample
 */
@KotventureDslMarker
public sealed interface EntitySelectorScope :
    PlayerEntitySelectorScope,
    SelfEntitySelectorScope {
    /**
     * Applies negated filters to this full entity selector.
     *
     * @sample io.github.lmliam.kotventure.core.selector.entitySelectorNegationSample
     */
    public fun not(init: NegatedEntitySelectorScope.() -> Unit)
}
