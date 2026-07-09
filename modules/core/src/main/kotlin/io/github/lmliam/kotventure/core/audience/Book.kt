package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.book.BookScope
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.inventory.Book
import io.github.lmliam.kotventure.core.book.book as buildBook

/**
 * Opens [book] for this [Audience], forwarding to Adventure's [Audience.openBook].
 *
 * @sample io.github.lmliam.kotventure.core.audience.audienceOpenBookSample
 */
public fun Audience.open(book: Book): Unit = openBook(book)

/**
 * Builds a [Book] from [init] and opens it on this [Audience].
 *
 * Works for any audience — a player, the console, or a forwarding audience over many members;
 * audiences without a book surface ignore it. For a reusable book shared across opens, prefer
 * [book][io.github.lmliam.kotventure.core.book.book] then [open].
 *
 * @throws IllegalStateException when `title` or `author` is set twice inside [init].
 * @sample io.github.lmliam.kotventure.core.audience.audienceBookSample
 */
public fun Audience.book(init: BookScope.() -> Unit) {
    open(buildBook(init))
}
