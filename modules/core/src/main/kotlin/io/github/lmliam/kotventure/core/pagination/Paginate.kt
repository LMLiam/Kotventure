package io.github.lmliam.kotventure.core.pagination

/**
 * Creates a [Pagination] snapshot of [items] with the rules in [init].
 *
 * The required renderer creates one component for each item. The function invokes it eagerly and in iteration order.
 * Each rendered page contains the applicable navigation controls.
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

/**
 * Creates a [Pagination] over two or more direct arguments. Refer to the [Iterable] overload of [paginate] for the
 * complete contract.
 *
 * Two leading parameters keep a single-argument call unambiguous: `paginate(collection) { }`
 * always paginates the collection's elements via the [Iterable] overload, never a single
 * collection-typed item. To paginate zero or one items, pass a list.
 *
 * @param T the type of the items being paginated.
 * @sample io.github.lmliam.kotventure.core.pagination.paginateSample
 *
 * @throws IllegalStateException when [init] leaves `renderer` unset, or sets any slot twice.
 * @throws IllegalArgumentException when [init] sets an invalid slot value, such as a non-positive
 *   `itemsPerPage`.
 */
public fun <T> paginate(
    first: T,
    second: T,
    vararg rest: T,
    init: PaginationScope<T>.() -> Unit,
): Pagination = buildPagination(listOf(first, second, *rest), init)

internal fun <T> buildPagination(
    items: Iterable<T>,
    init: PaginationScope<T>.() -> Unit,
): Pagination = PaginationBuilder<T>().apply(init).build(items)
