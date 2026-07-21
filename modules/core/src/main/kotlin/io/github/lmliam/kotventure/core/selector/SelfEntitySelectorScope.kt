package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.key.Key

/**
 * Provides arguments for the self-selector head.
 *
 * This scope supports entity-type filters. It does not support `limit` or `sort`.
 * A selector accepts one positive type or multiple excluded types. It does not accept both polarities.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selfEntitySelectorScopeSample
 */
@KotventureDslMarker
public sealed interface SelfEntitySelectorScope : CommonEntitySelectorScope {
    /**
     * Filters by entity type using an Adventure [Key]. Prefix the call with `!` to exclude it.
     *
     * @throws IllegalStateException when the selector already has a positive type, or when positive and negative type
     * filters are combined.
     * @sample io.github.lmliam.kotventure.core.selector.negatedTypeArgumentsSample
     */
    public fun type(entityType: Key): SelectorFilterExpression

    /**
     * Filters by entity type using a string.
     *
     * Preserves an identifier that has a namespace. Adds the `minecraft` namespace to a bare identifier. Prefix the
     * call with `!` to exclude the type.
     *
     * @throws IllegalArgumentException when [entityType] is not a valid Adventure key.
     * @throws IllegalStateException when the selector already has a positive type, or when positive and negative type
     * filters are combined.
     * @sample io.github.lmliam.kotventure.core.selector.negatedTypeArgumentsSample
     */
    public fun type(entityType: String): SelectorFilterExpression

    /**
     * Filters by an entity type tag using an Adventure [Key], rendering as `type=#namespace:tag`.
     * Prefix the call with `!` to exclude the tag.
     *
     * @throws IllegalStateException when the selector already has a positive type, or when positive and negative type
     * filters are combined.
     * @sample io.github.lmliam.kotventure.core.selector.negatedTypeArgumentsSample
     */
    public fun typeTag(entityTypeTag: Key): SelectorFilterExpression
}
