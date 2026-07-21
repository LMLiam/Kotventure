package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Provides arguments for player-selector heads.
 *
 * This scope supports `limit` and `sort`. It does not support entity-type filters. Singleton arguments can occur one
 * time.
 *
 * @sample io.github.lmliam.kotventure.core.selector.playerEntitySelectorScopeSample
 */
@KotventureDslMarker
public sealed interface PlayerEntitySelectorScope : CommonEntitySelectorScope {
    /** Sort by proximity (closest first). */
    public val nearest: SelectorSort

    /** Sort by distance (farthest first). */
    public val furthest: SelectorSort

    /** Sort randomly. */
    public val random: SelectorSort

    /** No guaranteed order. */
    public val arbitrary: SelectorSort

    /**
     * Sets the maximum number of matched entities.
     *
     * @throws IllegalArgumentException when [n] is not positive.
     * @throws IllegalStateException when `limit` is already set.
     */
    public fun limit(n: Int)

    /**
     * Sets the result order.
     *
     * @throws IllegalStateException when `sort` is already set.
     */
    public fun sort(sort: SelectorSort)
}
