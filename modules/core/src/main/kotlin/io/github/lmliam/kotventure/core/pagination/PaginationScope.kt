package io.github.lmliam.kotventure.core.pagination

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures a [Pagination] built via [paginate]: the required per-item [renderer], an optional
 * [header] shown above every page, the [itemsPerPage] page size, and the [nav] row.
 *
 * @param T the type of the items being paginated.
 * @sample io.github.lmliam.kotventure.core.pagination.paginateSample
 */
@KotventureDslMarker
public interface PaginationScope<T> {
    /**
     * Builds the header shown above every page from a component DSL block.
     *
     * @throws IllegalStateException when the header is already set in this block.
     */
    public fun header(init: ComponentScope.() -> Unit)

    /**
     * Sets the header shown above every page.
     *
     * @throws IllegalStateException when the header is already set in this block.
     */
    public fun <C : ComponentLike> header(component: C)

    /**
     * Sets how each item is rendered onto its page line. Required; every item is rendered once,
     * eagerly, when the pagination is built.
     *
     * @throws IllegalStateException when the renderer is already set in this block.
     */
    public fun renderer(render: (item: T) -> ComponentLike)

    /**
     * Sets how many items each page holds. Defaults to 6, which keeps a page with a header and a
     * nav row within the ten lines of a closed chat window.
     *
     * @throws IllegalStateException when the page size is already set in this block.
     * @throws IllegalArgumentException when [count] is not positive.
     */
    public fun itemsPerPage(count: Int)

    /**
     * Configures the nav row rendered under the items via [NavScope]: the prev/next button labels,
     * the page-position indicator, and the click-callback limits.
     *
     * When this block is omitted entirely, every [NavScope] default applies.
     *
     * @throws IllegalStateException when the nav row is already configured in this block, or when a
     *   nav slot is set twice inside [init].
     */
    public fun nav(init: NavScope.() -> Unit)
}
