package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Negated filters available to player-selector heads.
 *
 * @sample io.github.lmliam.kotventure.core.selector.negatedPlayerEntitySelectorScopeSample
 */
@KotventureDslMarker
public sealed interface NegatedPlayerEntitySelectorScope : NegatedCommonEntitySelectorScope
