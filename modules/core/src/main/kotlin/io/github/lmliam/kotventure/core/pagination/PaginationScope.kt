package io.github.lmliam.kotventure.core.pagination

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures the renderer, optional header, page size, and navigation row of a [Pagination].
 *
 * @param T the type of the items being paginated.
 * @sample io.github.lmliam.kotventure.core.pagination.paginateSample
 */
@KotventureDslMarker
public interface PaginationScope<T> {
    /**
     * Creates and sets the header shown above every page.
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
     * Sets how to render each item on its page line. This slot is required. The build renders each item one time.
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
