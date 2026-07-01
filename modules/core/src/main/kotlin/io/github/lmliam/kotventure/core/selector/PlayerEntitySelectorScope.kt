package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Scope for player-selector heads, which support ordering but not entity-type filters.
 *
 * @sample io.github.lmliam.kotventure.core.selector.playerEntitySelectorScopeSample
 */
@KotventureDslMarker
public interface PlayerEntitySelectorScope : CommonEntitySelectorScope {
    /** Sort by proximity (closest first). */
    public val nearest: SelectorSort

    /** Sort by distance (farthest first). */
    public val furthest: SelectorSort

    /** Sort randomly. */
    public val random: SelectorSort

    /** No guaranteed order. */
    public val arbitrary: SelectorSort

    /** Limits the number of matched entities. */
    public fun limit(n: Int)

    /** Sets the sort order for matched entities. */
    public fun sort(sort: SelectorSort)
}
