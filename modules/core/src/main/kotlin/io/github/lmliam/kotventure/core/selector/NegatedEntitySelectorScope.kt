package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.key.Key

/**
 * Negated filters for selector heads that support entity types.
 *
 * @sample io.github.lmliam.kotventure.core.selector.negatedEntitySelectorScopeSample
 */
@KotventureDslMarker
public sealed interface NegatedEntitySelectorScope : NegatedCommonEntitySelectorScope {
    /** Excludes an entity type using an Adventure [Key]. */
    public fun type(entityType: Key)

    /** Excludes an entity type, applying the `minecraft` namespace to a bare id. */
    public fun type(entityType: String)

    /** Excludes an entity type tag using an Adventure [Key]. */
    public fun typeTag(entityTypeTag: Key)
}
