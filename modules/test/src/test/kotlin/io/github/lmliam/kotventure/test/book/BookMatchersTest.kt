package io.github.lmliam.kotventure.test.book

import io.github.lmliam.kotventure.core.component.emptyComponent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.inventory.Book
import net.kyori.adventure.text.Component

class BookMatchersTest :
    StringSpec(
        {
            fun book(
                title: Component = emptyComponent(),
                author: Component = emptyComponent(),
                pages: List<Component> = emptyList(),
            ): Book = Book.book(title, author, pages)

            "matches title, author, page count, page at index, and full pages" {
                val title = Component.text("Rules")
                val author = Component.text("Staff")
                val page0 = Component.text("Be kind")
                val page1 = Component.text("No griefing")
                val subject = book(title, author, listOf(page0, page1))

                subject shouldHaveTitle title
                subject shouldHaveAuthor author
                subject shouldHavePageCount 2
                subject.shouldHavePageAt(0, page0)
                subject.shouldHavePageAt(1, page1)
                subject shouldHavePages listOf(page0, page1)
                subject shouldNotHaveTitle Component.text("Other")
                subject shouldNotHaveAuthor Component.text("Other")
                subject shouldNotHavePageCount 1
                subject.shouldNotHavePageAt(0, page1)
                subject shouldNotHavePages listOf(page1, page0)
            }

            "matches an empty book" {
                val subject = book()
                subject shouldHaveTitle Component.empty()
                subject shouldHaveAuthor Component.empty()
                subject shouldHavePageCount 0
                subject shouldHavePages emptyList()
            }

            "reports a title mismatch with expected and actual values" {
                val failure =
                    shouldThrow<AssertionError> {
                        book(title = Component.text("A")) shouldHaveTitle Component.text("B")
                    }

                failure.message shouldContain "Expected book title"
                failure.message shouldContain "A"
                failure.message shouldContain "B"
            }

            "reports when title unexpectedly matches" {
                val title = Component.text("Rules")
                val failure =
                    shouldThrow<AssertionError> {
                        book(title = title) shouldNotHaveTitle title
                    }

                failure.message shouldContain "Expected book title not to be"
            }

            "reports an author mismatch with expected and actual values" {
                val failure =
                    shouldThrow<AssertionError> {
                        book(author = Component.text("A")) shouldHaveAuthor
                                Component.text("B")
                    }

                failure.message shouldContain "Expected book author"
            }

            "reports a page count mismatch" {
                val failure =
                    shouldThrow<AssertionError> {
                        book(pages = listOf(Component.text("p"))) shouldHavePageCount 2
                    }

                failure.message shouldContain
                        "Expected book page count <2>, but was <1>."
            }

            "reports a page-at-index mismatch" {
                val failure =
                    shouldThrow<AssertionError> {
                        book(pages = listOf(Component.text("A"))).shouldHavePageAt(
                            0,
                            Component.text("B"),
                        )
                    }

                failure.message shouldContain "Expected book page at index <0>"
            }

            "reports a missing page index" {
                val failure =
                    shouldThrow<AssertionError> {
                        book().shouldHavePageAt(0, Component.text("x"))
                    }

                failure.message shouldContain
                        "Expected book page at index <0>, but page count was <0>."
            }

            "reports a pages mismatch" {
                val failure =
                    shouldThrow<AssertionError> {
                        book(pages = listOf(Component.text("A"))) shouldHavePages
                                listOf(Component.text("B"))
                    }

                failure.message shouldContain "Expected book pages"
            }
        },
    )
