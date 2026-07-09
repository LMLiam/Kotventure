package io.github.lmliam.kotventure.core.book

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.book.shouldHaveAuthor
import io.github.lmliam.kotventure.test.book.shouldHavePageCount
import io.github.lmliam.kotventure.test.book.shouldHavePages
import io.github.lmliam.kotventure.test.book.shouldHaveTitle
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component

class BookDslTest :
    StringSpec(
        {
            "builds a fully configured book" {
                val built =
                    book {
                        title {
                            text("Server Rules") { color(gold) }
                        }
                        author { text("Staff") }
                        page {
                            text("1. Be kind")
                            newline()
                            text("2. No griefing")
                        }
                        page { text("Contact staff for help.") }
                    }

                built.title().childAt(0) shouldContainText "Server Rules"
                built.title().childAt(0) shouldHaveColor gold
                built.author().childAt(0) shouldContainText "Staff"
                built shouldHavePageCount 2
                built.pages()[0].childAt(0) shouldContainText "1. Be kind"
                built.pages()[1].childAt(0) shouldContainText "Contact staff for help."
            }

            "defaults produce empty title, author, and pages" {
                val built = book {}

                built shouldHaveTitle Component.empty()
                built shouldHaveAuthor Component.empty()
                built shouldHavePageCount 0
                built shouldHavePages emptyList()
            }

            "accepts ComponentLike overloads for title, author, and page" {
                val title = Component.text("T")
                val author = Component.text("A")
                val page = Component.text("P")

                val built =
                    book {
                        title(title)
                        author(author)
                        page(page)
                    }

                built shouldHaveTitle title
                built shouldHaveAuthor author
                built shouldHavePages listOf(page)
            }

            "bulk pages preserve order after earlier page calls" {
                val first = Component.text("first")
                val second = Component.text("second")
                val third = Component.text("third")

                val fromVararg =
                    book {
                        page(first)
                        pages(second, third)
                    }
                fromVararg shouldHavePages listOf(first, second, third)

                val fromIterable =
                    book {
                        page(first)
                        pages(listOf(second, third))
                    }
                fromIterable shouldHavePages listOf(first, second, third)
            }

            "rejects a second title" {
                val failure =
                    shouldThrow<IllegalStateException> {
                        book {
                            title { text("A") }
                            title { text("B") }
                        }
                    }
                failure.message shouldBe "'title' is already set."
            }

            "rejects a second author" {
                val failure =
                    shouldThrow<IllegalStateException> {
                        book {
                            author { text("A") }
                            author { text("B") }
                        }
                    }
                failure.message shouldBe "'author' is already set."
            }
        },
    )
