package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Complete negated filter scope for full entity-selector heads.
 *
 * @sample io.github.lmliam.kotventure.core.selector.negatedEntitySelectorScopeSample
 */
@KotventureDslMarker
public sealed interface NegatedEntitySelectorScope : NegatedSelfEntitySelectorScope
