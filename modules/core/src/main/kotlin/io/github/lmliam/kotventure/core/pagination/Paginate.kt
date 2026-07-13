package io.github.lmliam.kotventure.core.pagination

/**
 * Builds a [Pagination] over [items] configured by [init]: each item is rendered onto a page line
 * by the block's required `renderer`, and every rendered page carries clickable prev/next
 * navigation.
 *
 * Items are rendered eagerly, so the pagination is a snapshot of [items] at build time.
 *
 * @param T the type of the items being paginated.
 * @sample io.github.lmliam.kotventure.core.pagination.paginateSample
 *
 * @throws IllegalStateException when [init] leaves `renderer` unset, or sets any slot twice.
 * @throws IllegalArgumentException when [init] sets an invalid slot value, such as a non-positive
 *   `itemsPerPage`.
 */
public fun <T> paginate(
    items: Iterable<T>,
    init: PaginationScope<T>.() -> Unit,
): Pagination = buildPagination(items, init)

internal fun <T> buildPagination(
    items: Iterable<T>,
    init: PaginationScope<T>.() -> Unit,
): Pagination = PaginationBuilder<T>().apply(init).build(items)
