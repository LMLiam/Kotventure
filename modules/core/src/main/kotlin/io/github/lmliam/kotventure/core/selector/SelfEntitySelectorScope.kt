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
    /**
     * Filters by entity type using an Adventure [Key]. Prefix the call with `!` to exclude it.
     *
     * @sample io.github.lmliam.kotventure.core.selector.negatedTypeArgumentsSample
     */
    public fun type(entityType: Key): SelectorFilterExpression

    /**
     * Filters by entity type using a string.
     *
     * An already-namespaced id is preserved; a bare id uses the `minecraft` namespace.
     * Prefix the call with `!` to exclude the type.
     *
     * @sample io.github.lmliam.kotventure.core.selector.negatedTypeArgumentsSample
     */
    public fun type(entityType: String): SelectorFilterExpression

    /**
     * Filters by an entity type tag using an Adventure [Key], rendering as `type=#namespace:tag`.
     * Prefix the call with `!` to exclude the tag.
     *
     * @sample io.github.lmliam.kotventure.core.selector.negatedTypeArgumentsSample
     */
    public fun typeTag(entityTypeTag: Key): SelectorFilterExpression
}
