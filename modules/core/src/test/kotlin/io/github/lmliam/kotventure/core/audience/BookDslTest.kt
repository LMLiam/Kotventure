package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.book.book
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.book.shouldHavePageCount
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.inventory.Book

private class BookRecordingAudience : Audience {
    val opened = mutableListOf<Book>()

    override fun openBook(book: Book) {
        opened += book
    }
}

class BookDslTest :
    StringSpec(
        {
            "opens a prebuilt book" {
                val audience = BookRecordingAudience()
                val rules =
                    book {
                        title { text("Rules") }
                        page { text("Be kind") }
                    }

                audience.open(rules)

                audience.opened shouldHaveSize 1
                audience.opened.single() shouldBe rules
            }

            "builds and opens a book in one expression" {
                val audience = BookRecordingAudience()

                audience.book {
                    title { text("Welcome") }
                    page { text("Hello!") }
                }

                audience.opened shouldHaveSize 1
                val opened = audience.opened.single()
                opened.title().childAt(0) shouldContainText "Welcome"
                opened shouldHavePageCount 1
                opened.pages().single().childAt(0) shouldContainText "Hello!"
            }

            "forwards open to every member of a composite audience" {
                val first = BookRecordingAudience()
                val second = BookRecordingAudience()
                val rules =
                    book {
                        page { text("Shared") }
                    }

                audienceOf(first, second).open(rules)

                first.opened shouldHaveSize 1
                second.opened shouldHaveSize 1
                first.opened.single() shouldBe rules
                second.opened.single() shouldBe rules
            }

            "forwards book build-and-open to every member" {
                val first = BookRecordingAudience()
                val second = BookRecordingAudience()

                audienceOf(first, second).book {
                    page { text("Hello") }
                }

                first.opened shouldHaveSize 1
                second.opened shouldHaveSize 1
                first.opened.single() shouldBe second.opened.single()
            }
        },
    )
