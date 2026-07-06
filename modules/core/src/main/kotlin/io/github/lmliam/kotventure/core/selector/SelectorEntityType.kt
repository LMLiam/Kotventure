package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

/**
 * The target of a `type=` selector argument: a concrete entity type or an entity-type tag.
 */
public sealed interface SelectorEntityType {
    /** Entity type or entity-type-tag key. */
    public val key: Key

    /**
     * A concrete entity type such as `minecraft:zombie`.
     *
     * @property key entity type key
     */
    public data class Direct(
        override val key: Key,
    ) : SelectorEntityType

    /**
     * An entity-type tag such as `#minecraft:raiders`.
     *
     * @property key entity-type tag key
     */
    public data class Tag(
        override val key: Key,
    ) : SelectorEntityType
}
