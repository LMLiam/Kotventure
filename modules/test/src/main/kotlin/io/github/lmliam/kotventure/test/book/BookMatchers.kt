package io.github.lmliam.kotventure.test.book

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component

/**
 * Matches a [Book] whose title equals [expected].
 */
public fun haveTitle(expected: Component): Matcher<Book> =
    Matcher { value ->
        val actual = value.title()
        MatcherResult(
            actual == expected,
            { "Expected book title <$expected>, but was <$actual>." },
            { "Expected book title not to be <$expected>." },
        )
    }

/**
 * Matches a [Book] whose author equals [expected].
 */
public fun haveAuthor(expected: Component): Matcher<Book> =
    Matcher { value ->
        val actual = value.author()
        MatcherResult(
            actual == expected,
            { "Expected book author <$expected>, but was <$actual>." },
            { "Expected book author not to be <$expected>." },
        )
    }

/**
 * Matches a [Book] with exactly [expected] pages.
 */
public fun havePageCount(expected: Int): Matcher<Book> =
    Matcher { value ->
        val actual = value.pages().size
        MatcherResult(
            actual == expected,
            { "Expected book page count <$expected>, but was <$actual>." },
            { "Expected book page count not to be <$expected>." },
        )
    }

/**
 * Matches a [Book] whose page at [index] equals [expected].
 */
public fun havePageAt(
    index: Int,
    expected: Component,
): Matcher<Book> =
    Matcher { value ->
        val pages = value.pages()
        if (index !in pages.indices) {
            MatcherResult(
                false,
                {
                    "Expected book page at index <$index>, but page count was <${pages.size}>."
                },
                { "Expected book not to have page at index <$index>." },
            )
        } else {
            val actual = pages[index]
            MatcherResult(
                actual == expected,
                {
                    "Expected book page at index <$index> to be <$expected>, but was <$actual>."
                },
                {
                    "Expected book page at index <$index> not to be <$expected>."
                },
            )
        }
    }

/**
 * Matches a [Book] whose pages equal [expected] in order.
 */
public fun havePages(expected: List<Component>): Matcher<Book> =
    Matcher { value ->
        val actual = value.pages()
        MatcherResult(
            actual == expected,
            { "Expected book pages <$expected>, but was <$actual>." },
            { "Expected book pages not to be <$expected>." },
        )
    }

/**
 * Asserts this [Book] has title [expected].
 */
public infix fun Book.shouldHaveTitle(expected: Component): Book =
    apply {
        this should haveTitle(expected)
    }

/**
 * Asserts this [Book] does not have title [expected].
 */
public infix fun Book.shouldNotHaveTitle(expected: Component): Book =
    apply {
        this shouldNot haveTitle(expected)
    }

/**
 * Asserts this [Book] has author [expected].
 */
public infix fun Book.shouldHaveAuthor(expected: Component): Book =
    apply {
        this should haveAuthor(expected)
    }

/**
 * Asserts this [Book] does not have author [expected].
 */
public infix fun Book.shouldNotHaveAuthor(expected: Component): Book =
    apply {
        this shouldNot haveAuthor(expected)
    }

/**
 * Asserts this [Book] has exactly [expected] pages.
 */
public infix fun Book.shouldHavePageCount(expected: Int): Book =
    apply {
        this should havePageCount(expected)
    }

/**
 * Asserts this [Book] does not have exactly [expected] pages.
 */
public infix fun Book.shouldNotHavePageCount(expected: Int): Book =
    apply {
        this shouldNot havePageCount(expected)
    }

/**
 * Asserts this [Book] has page [expected] at [index].
 */
public fun Book.shouldHavePageAt(
    index: Int,
    expected: Component,
): Book =
    apply {
        this should havePageAt(index, expected)
    }

/**
 * Asserts this [Book] does not have page [expected] at [index].
 */
public fun Book.shouldNotHavePageAt(
    index: Int,
    expected: Component,
): Book =
    apply {
        this shouldNot havePageAt(index, expected)
    }

/**
 * Asserts this [Book] has pages [expected] in order.
 */
public infix fun Book.shouldHavePages(expected: List<Component>): Book =
    apply {
        this should havePages(expected)
    }

/**
 * Asserts this [Book] does not have pages [expected] in order.
 */
public infix fun Book.shouldNotHavePages(expected: List<Component>): Book =
    apply {
        this shouldNot havePages(expected)
    }
