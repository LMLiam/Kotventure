package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.key.Key

/**
 * Scope for configuring entity selector arguments.
 *
 * ```kotlin
 * entities {
 *     type("armor_stand")
 *     distance(atMost(10.0))
 *     sort(nearest)
 *     limit(1)
 *     tag("display")
 * }
 * ```
 */
@KotventureDslMarker
public interface EntitySelectorScope {
    /** Sort by proximity (closest first). */
    public val nearest: SelectorSort

    /** Sort by distance (farthest first). */
    public val furthest: SelectorSort

    /** Sort randomly. */
    public val random: SelectorSort

    /** No guaranteed order. */
    public val arbitrary: SelectorSort

    /**
     * Filters by entity type using an Adventure [Key].
     */
    public fun type(entityType: Key)

    /**
     * Filters by entity type using a string (implies `minecraft` namespace).
     */
    public fun type(entityType: String)

    /**
     * Limits the number of matched entities.
     */
    public fun limit(n: Int)

    /**
     * Filters by distance using a [SelectorRange].
     */
    public fun distance(range: SelectorRange)

    /**
     * Filters by distance using a Kotlin [ClosedFloatingPointRange].
     */
    public fun distance(range: ClosedFloatingPointRange<Double>)

    /**
     * Sets the sort order for matched entities.
     */
    public fun sort(sort: SelectorSort)

    /**
     * Filters by scoreboard tag.
     */
    public fun tag(tag: String)

    /**
     * Filters by entity name.
     */
    public fun name(name: String)

    /**
     * Filters by experience level using a [SelectorRange].
     */
    public fun level(range: SelectorRange)

    /**
     * Filters by game mode.
     */
    public fun gamemode(mode: String)
}
