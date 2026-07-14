package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.pagination.Pagination
import io.github.lmliam.kotventure.core.pagination.PaginationScope
import io.github.lmliam.kotventure.core.pagination.buildPagination
import net.kyori.adventure.audience.Audience

/**
 * Builds a [Pagination] over [items] configured by [init] and sends its first page to this
 * [Audience] as a system chat message.
 *
 * The sent page is self-navigating: its prev/next buttons send the target page to whichever
 * audience clicks them. To start elsewhere than the first page, or to keep the pagination around,
 * build it with [paginate][io.github.lmliam.kotventure.core.pagination.paginate] and send a
 * rendered page yourself.
 *
 * @param T the type of the items being paginated.
 * @sample io.github.lmliam.kotventure.core.pagination.audiencePaginateSample
 *
 * @throws IllegalStateException when [init] leaves `renderer` unset, or sets any slot twice.
 * @throws IllegalArgumentException when [init] sets an invalid slot value, such as a non-positive
 *   `itemsPerPage`.
 */
public fun <T> Audience.paginate(
    items: Iterable<T>,
    init: PaginationScope<T>.() -> Unit,
): Unit = sendMessage(buildPagination(items, init).page(1))

/**
 * Builds a [Pagination] over two or more items given directly as arguments and sends its first
 * page to this [Audience]; see the [Iterable] overload of [paginate] for the full contract.
 *
 * Two leading parameters keep a single-argument call unambiguous: `paginate(collection) { }`
 * always paginates the collection's elements via the [Iterable] overload, never a single
 * collection-typed item. To paginate zero or one items, pass a list.
 *
 * @param T the type of the items being paginated.
 * @sample io.github.lmliam.kotventure.core.pagination.audiencePaginateSample
 *
 * @throws IllegalStateException when [init] leaves `renderer` unset, or sets any slot twice.
 * @throws IllegalArgumentException when [init] sets an invalid slot value, such as a non-positive
 *   `itemsPerPage`.
 */
public fun <T> Audience.paginate(
    first: T,
    second: T,
    vararg rest: T,
    init: PaginationScope<T>.() -> Unit,
): Unit = sendMessage(buildPagination(listOf(first, second, *rest), init).page(1))
