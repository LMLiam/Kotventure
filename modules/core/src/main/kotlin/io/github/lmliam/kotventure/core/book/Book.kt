package io.github.lmliam.kotventure.core.book

import net.kyori.adventure.inventory.Book

/**
 * Builds an Adventure [Book] from a [BookScope] block without opening it.
 *
 * Use this when one book is shared across audiences or held for later
 * [open][io.github.lmliam.kotventure.core.audience.open]. For a one-shot build-and-open, prefer
 * [Audience.book][io.github.lmliam.kotventure.core.audience.book].
 *
 * An unset title or author becomes an empty component. Pages can be empty. You can set the title and author one time.
 *
 * @throws IllegalStateException when `title` or `author` is set twice.
 * @sample io.github.lmliam.kotventure.core.book.bookSample
 */
public fun book(init: BookScope.() -> Unit): Book = BookBuilder().apply(init).build()
