package io.github.lmliam.kotventure.core.pagination

import io.github.lmliam.kotventure.core.event.buildClickEvent
import io.github.lmliam.kotventure.core.text.join
import net.kyori.adventure.text.Component

/**
 * A paginated view over pre-rendered items, produced by [paginate].
 *
 * Pages are self-navigating: [page] returns a chat-ready [Component] whose prev/next buttons carry
 * server-side click callbacks that render the target page and send it to whichever
 * [audience][net.kyori.adventure.audience.Audience] clicked them — no platform wiring needed.
 *
 * @sample io.github.lmliam.kotventure.core.pagination.paginateSample
 */
public class Pagination internal constructor(
    private val items: List<Component>,
    private val header: Component?,
    private val itemsPerPage: Int,
    private val nav: NavSettings,
) {
    /**
     * The number of pages this pagination renders — always at least 1; an empty item list renders
     * a single page with no items.
     */
    public val pageCount: Int =
        if (items.isEmpty()) 1 else (items.size + itemsPerPage - 1) / itemsPerPage

    /**
     * Renders [page] as a single component: the header (when set), the page's items each on their
     * own line, and the nav row — the previous button (absent on the first page), the
     * page-position indicator (unless hidden), and the next button (absent on the last page).
     *
     * @throws IllegalArgumentException when [page] is outside `1..pageCount`.
     */
    public fun page(page: Int): Component {
        require(page in 1..pageCount) { "'page' must be in 1..$pageCount, was $page." }
        return listOfNotNull(header, body(page), navRow(page))
                .join { separator(Component.newline()) }
    }

    private fun body(page: Int): Component? {
        if (items.isEmpty()) return null
        val from = (page - 1) * itemsPerPage
        val to = minOf(from + itemsPerPage, items.size)
        return items.subList(from, to).join { separator(Component.newline()) }
    }

    private fun navRow(page: Int): Component? {
        val parts =
            listOfNotNull(
                    previousButton(page),
                    nav.indicator(page, pageCount)?.asComponent(),
                    nextButton(page),
            )
        return if (parts.isEmpty()) null else parts.join { separator(" ") }
    }

    private fun previousButton(page: Int): Component? = if (page == 1) null else navButton(nav.previous, page - 1)

    private fun nextButton(page: Int): Component? = if (page == pageCount) null else navButton(nav.next, page + 1)

    private fun navButton(
        label: Component,
        target: Int,
    ): Component =
        label.clickEvent(
            buildClickEvent {
                callback(nav.uses, nav.lifetime) { audience -> audience.sendMessage(page(target)) }
            },
        )
}
