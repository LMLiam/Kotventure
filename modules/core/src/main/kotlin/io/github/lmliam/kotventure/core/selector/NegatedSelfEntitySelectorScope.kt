package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.key.Key

/**
 * Negated filters available to the self-selector head.
 *
 * @sample io.github.lmliam.kotventure.core.selector.negatedSelfEntitySelectorScopeSample
 */
@KotventureDslMarker
public sealed interface NegatedSelfEntitySelectorScope : NegatedCommonEntitySelectorScope {
    /** Excludes an entity type using an Adventure [Key]. */
    public fun type(entityType: Key)

    /** Excludes an entity type, applying the `minecraft` namespace to a bare id. */
    public fun type(entityType: String)

    /** Excludes an entity type tag using an Adventure [Key]. */
    public fun typeTag(entityTypeTag: Key)
}
