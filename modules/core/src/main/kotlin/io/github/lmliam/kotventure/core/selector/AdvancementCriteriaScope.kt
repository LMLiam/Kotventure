package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Criteria for one advancement selector filter.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorAdvancementSample
 */
@KotventureDslMarker
public sealed interface AdvancementCriteriaScope {
    /**
     * Requires the named criterion to have the requested completion state.
     *
     * Repeated names replace their value without changing insertion order.
     *
     * @throws IllegalArgumentException if [name] is empty or is not a vanilla unquoted token
     */
    public fun criterion(
        name: String,
        completed: Boolean,
    )
}
