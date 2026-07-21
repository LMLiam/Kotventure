package io.github.lmliam.kotventure.core.pagination

import io.github.lmliam.kotventure.core.component.newlineComponent
import io.github.lmliam.kotventure.core.event.buildClickEvent
import io.github.lmliam.kotventure.core.text.join
import net.kyori.adventure.text.Component

/**
 * An immutable paginated view of pre-rendered item components.
 *
 * [page] returns a chat-ready [Component] with previous and next buttons. Their
 * server-side click callbacks render the target page and send it to the
 * [audience][net.kyori.adventure.audience.Audience] that selected the button. No platform configuration is necessary.
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
     * The number of pages that this pagination renders. The value is at least 1. An empty item list renders one page
     * with no items.
     */
    public val pageCount: Int =
        if (items.isEmpty()) 1 else (items.size + itemsPerPage - 1) / itemsPerPage

    /**
     * Creates one component for page number [page]. It contains the optional header, one line per item, and the
     * navigation row.
     * The first page has no previous button, and the last page has no next button. The row omits the page-position
     * indicator when it is hidden. Each call creates new click callbacks with fresh use and lifetime limits.
     *
     * @throws IllegalArgumentException when [page] is outside `1..pageCount`.
     */
    public fun page(page: Int): Component {
        require(page in 1..pageCount) { "'page' must be in 1..$pageCount, was $page." }
        return listOfNotNull(header, body(page), navRow(page))
            .join { separator(newlineComponent()) }
    }

    private fun body(page: Int): Component? {
        if (items.isEmpty()) return null
        val from = (page - 1) * itemsPerPage
        val to = minOf(from + itemsPerPage, items.size)
        return items.subList(from, to).join { separator(newlineComponent()) }
    }

    private fun navRow(page: Int): Component? =
        listOfNotNull(
            previousButton(page),
            nav.indicator(page, pageCount)?.asComponent(),
            nextButton(page),
        ).takeIf { it.isNotEmpty() }
            ?.join { separator(" ") }

    private fun previousButton(page: Int): Component? = if (page == 1) null else navButton(nav.previous, page - 1)

    private fun nextButton(page: Int): Component? = if (page == pageCount) null else navButton(nav.next, page + 1)

    private fun navButton(
        label: Component,
        target: Int,
    ): Component =
        label.clickEvent(
            buildClickEvent {
                callback(nav.uses, nav.lifetime) { audience ->
                    audience.sendMessage(page(target))
                }
            },
        )
}
