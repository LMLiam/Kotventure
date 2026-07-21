package io.github.lmliam.kotventure.core.selector

/**
 * The result order for an entity selector.
 *
 * Access via scoped constants inside sortable selector scopes (see [PlayerEntitySelectorScope]): `sort(nearest)`.
 */
public enum class SelectorSort(
    internal val value: String,
) {
    /** Sorts by increasing distance from the selector origin. */
    NEAREST("nearest"),

    /** Sorts by decreasing distance from the selector origin. */
    FURTHEST("furthest"),

    /** Sorts the results in a random order. */
    RANDOM("random"),

    /** Does not request a defined result order. */
    ARBITRARY("arbitrary"),
}
