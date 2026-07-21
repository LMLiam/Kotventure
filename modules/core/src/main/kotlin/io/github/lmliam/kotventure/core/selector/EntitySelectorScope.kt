package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Provides all typed entity-selector arguments.
 *
 * The [entities] and [nearestEntity] factories use this scope. Singleton arguments can occur one time. Repeatable
 * filters keep call order. Invalid values fail when the scope receives them. Invalid filter combinations fail no later
 * than the end of the selector block.
 *
 * @sample io.github.lmliam.kotventure.core.selector.entitySelectorScopeSample
 */
@KotventureDslMarker
public sealed interface EntitySelectorScope :
    PlayerEntitySelectorScope,
    SelfEntitySelectorScope
