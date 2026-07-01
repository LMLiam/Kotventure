package io.github.lmliam.kotventure.core.selector

/**
 * Sort order for entity selectors.
 *
 * Access via scoped constants inside sortable selector scopes (see [PlayerEntitySelectorScope]): `sort(nearest)`.
 */
public enum class SelectorSort(
    internal val value: String,
) {
    NEAREST("nearest"),
    FURTHEST("furthest"),
    RANDOM("random"),
    ARBITRARY("arbitrary"),
}
