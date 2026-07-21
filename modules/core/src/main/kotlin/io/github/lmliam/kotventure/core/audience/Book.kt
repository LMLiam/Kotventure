package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.book.BookScope
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.inventory.Book
import io.github.lmliam.kotventure.core.book.book as buildBook

/**
 * Opens [book] for this [Audience] through [Audience.openBook].
 *
 * An audience without a book surface ignores the operation.
 *
 * @sample io.github.lmliam.kotventure.core.audience.audienceOpenBookSample
 */
public fun Audience.open(book: Book): Unit = openBook(book)

/**
 * Creates a [Book] from [init] and opens it for this [Audience].
 *
 * Works for a player, the console, or a forwarding audience. An audience without a book surface ignores it. For a
 * reusable book that you open more than once, use
 * [book][io.github.lmliam.kotventure.core.book.book] then [open].
 *
 * @throws IllegalStateException when `title` or `author` is set twice inside [init].
 * @sample io.github.lmliam.kotventure.core.audience.audienceBookSample
 */
public fun Audience.book(init: BookScope.() -> Unit) {
    open(buildBook(init))
}
