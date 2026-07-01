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

    /** Filters by an entity type tag using an Adventure [Key], rendering as `type=#namespace:tag`. */
    public fun typeTag(entityTypeTag: Key)

    /**
     * Excludes an entity type tag: `typeTag(!key("minecraft", "raiders"))`. Exclusions accumulate.
     *
     * @sample io.github.lmliam.kotventure.core.selector.negatedTypeArgumentsSample
     */
    public fun typeTag(entityTypeTag: Excluded<Key>)

    /** Marks a [Key] argument value as excluded: `type(!key("minecraft", "zombie"))`. */
    public operator fun Key.not(): Excluded<Key> = Excluded(this)
}

// The negated type overloads live outside the interface: Excluded<Key> and Excluded<String> erase
// to the same JVM signature, and @JvmName cannot disambiguate overridable members. The casts are
// safe because the sealed hierarchy's only implementation is EntitySelectorAdapter.

/**
 * Excludes an entity type using an Adventure [Key]: `type(!key("minecraft", "zombie"))`.
 * Exclusions accumulate.
 *
 * @sample io.github.lmliam.kotventure.core.selector.negatedTypeArgumentsSample
 */
public fun SelfEntitySelectorScope.type(entityType: Excluded<Key>) {
    (this as EntitySelectorAdapter).state.excludeType(entityType.value)
}

/**
 * Excludes an entity type by string, applying the `minecraft` namespace to a bare id:
 * `type(!"zombie")`. Exclusions accumulate.
 *
 * @sample io.github.lmliam.kotventure.core.selector.negatedTypeArgumentsSample
 */
@JvmName("typeExcludedByString")
public fun SelfEntitySelectorScope.type(entityType: Excluded<String>) {
    (this as EntitySelectorAdapter).state.excludeType(entityType.value)
}
