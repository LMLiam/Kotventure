package io.github.lmliam.kotventure.core.book

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.ComponentLike

/**
 * Configures the optional [title], optional [author], and ordered pages of an Adventure [Book].
 *
 * Unset title and author default to [net.kyori.adventure.text.Component.empty]. Pages accumulate
 * in call order via [page] or bulk [pages]. Title and author may each be set at most once.
 *
 * @sample io.github.lmliam.kotventure.core.book.bookSample
 */
@KotventureDslMarker
public interface BookScope {
    /**
     * Creates and sets the book title from a component DSL block.
     *
     * @throws IllegalStateException when the title is already set in this block.
     */
    public fun title(init: ComponentScope.() -> Unit)

    /**
     * Sets the book title.
     *
     * @throws IllegalStateException when the title is already set in this block.
     */
    public fun <T : ComponentLike> title(component: T)

    /**
     * Creates and sets the book author from a component DSL block.
     *
     * @throws IllegalStateException when the author is already set in this block.
     */
    public fun author(init: ComponentScope.() -> Unit)

    /**
     * Sets the book author.
     *
     * @throws IllegalStateException when the author is already set in this block.
     */
    public fun <T : ComponentLike> author(component: T)

    /**
     * Creates a page from a component DSL block and appends it.
     *
     * Pages are kept in call order. Components that exceed the client page limit are truncated
     * client-side (Adventure does not reflow to the next page).
     */
    public fun page(init: ComponentScope.() -> Unit)

    /**
     * Appends [component] as a page.
     *
     * Pages are kept in call order.
     */
    public fun <T : ComponentLike> page(component: T)

    /**
     * Appends each of [pages] as a page, in order.
     */
    public fun pages(vararg pages: ComponentLike)

    /**
     * Appends each of [pages] as a page, in iteration order.
     */
    public fun pages(pages: Iterable<ComponentLike>)
}
