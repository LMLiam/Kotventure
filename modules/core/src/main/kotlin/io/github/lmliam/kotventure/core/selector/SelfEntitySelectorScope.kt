package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.key.Key

/**
 * Scope for the self-selector head, which supports entity-type filters but not ordering.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selfEntitySelectorScopeSample
 */
@KotventureDslMarker
public sealed interface SelfEntitySelectorScope : CommonEntitySelectorScope {
    /** Filters by entity type using an Adventure [Key]. */
    public fun type(entityType: Key)

    /**
     * Filters by entity type using a string.
     *
     * An already-namespaced id is preserved; a bare id uses the `minecraft` namespace.
     */
    public fun type(entityType: String)

    /** Filters by an entity type tag using an Adventure [Key]. */
    public fun typeTag(entityTypeTag: Key)
}
