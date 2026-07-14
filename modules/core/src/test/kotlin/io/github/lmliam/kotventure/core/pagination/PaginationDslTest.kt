package io.github.lmliam.kotventure.core.pagination

import io.github.lmliam.kotventure.core.audience.paginate
import io.github.lmliam.kotventure.core.event.RecordingClickCallbackProvider
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.haveNoClickEvent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveClickEvent
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.github.lmliam.kotventure.test.text.shouldNotContainText
import io.github.lmliam.kotventure.test.text.shouldNotHaveClickEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes
import java.time.Duration as JavaDuration

private class RecordingAudience : Audience {
    val messages = mutableListOf<Component>()

    override fun sendMessage(message: Component) {
        messages += message
    }
}

class PaginationDslTest :
    StringSpec(
        {
            "renders header, items, indicator, and next button on the first page" {
                val pagination =
                    paginate("Alpha", "Beta", "Gamma", "Delta") {
                        header { text("Warps") }
                        renderer { text(it) }
                        itemsPerPage(2)
                    }

                val page = pagination.page(1)
                page shouldHaveChildCount 5
                page.childAt(0) shouldHaveContent "Warps"
                page.childAt(2) shouldHaveContent "Alpha\nBeta"
                page.childAt(4) shouldHaveContent "1/2 Next »"
            }

            "accepts a prebuilt header component" {
                val pagination =
                    paginate(listOf("Alpha")) {
                        header(text("Warps"))
                        renderer { text(it) }
                    }

                pagination.page(1).childAt(0) shouldHaveContent "Warps"
            }

            "slices items onto later pages" {
                val pagination =
                    paginate(listOf("Alpha", "Beta", "Gamma", "Delta")) {
                        renderer { text(it) }
                        itemsPerPage(2)
                    }

                val page = pagination.page(2)
                page shouldContainText "Gamma"
                page shouldContainText "Delta"
                page shouldNotContainText "Alpha"
                page shouldNotContainText "Beta"
            }

            "computes the page count from itemsPerPage" {
                val pagination =
                    paginate(listOf("a", "b", "c", "d", "e")) {
                        renderer { text(it) }
                        itemsPerPage(2)
                    }

                pagination.pageCount shouldBe 3
            }

            "defaults to six items per page" {
                val pagination =
                    paginate((1..7).toList()) {
                        renderer { text("item $it") }
                    }

                pagination.pageCount shouldBe 2
                val page = pagination.page(1)
                page shouldContainText "item 6"
                page shouldNotContainText "item 7"
            }

            "renders a single page with no nav buttons" {
                val pagination =
                    paginate(listOf("Alpha", "Beta")) {
                        renderer { text(it) }
                    }

                pagination.pageCount shouldBe 1
                pagination.page(1) shouldHaveContent "Alpha\nBeta\n1/1"
            }

            "omits prev on the first page and next on the last" {
                val pagination =
                    paginate(listOf("Alpha", "Beta", "Gamma")) {
                        renderer { text(it) }
                        itemsPerPage(1)
                    }

                pagination.page(1) shouldNotContainText "Previous"
                pagination.page(1) shouldContainText "Next »"
                pagination.page(2) shouldContainText "« Previous"
                pagination.page(2) shouldContainText "Next »"
                pagination.page(3) shouldContainText "« Previous"
                pagination.page(3) shouldNotContainText "Next"
            }

            "renders an empty items list as a single page" {
                val pagination =
                    paginate(emptyList<String>()) {
                        header { text("Warps") }
                        renderer { text(it) }
                    }

                pagination.pageCount shouldBe 1
                pagination.page(1) shouldHaveContent "Warps\n1/1"
            }

            "attaches a click event to each nav button but not the indicator" {
                RecordingClickCallbackProvider.reset()
                val pagination =
                    paginate(listOf("Alpha", "Beta", "Gamma")) {
                        renderer { text(it) }
                        itemsPerPage(1)
                    }

                val nav = pagination.page(2).childAt(2)
                nav shouldHaveChildCount 5
                nav.childAt(0) shouldNot haveNoClickEvent()
                nav.childAt(2).shouldNotHaveClickEvent()
                nav.childAt(4) shouldHaveClickEvent RecordingClickCallbackProvider.lastEvent
            }

            "next button sends the following page to the clicking audience" {
                RecordingClickCallbackProvider.reset()
                val pagination =
                    paginate(listOf("Alpha", "Beta", "Gamma")) {
                        renderer { text(it) }
                        itemsPerPage(1)
                    }
                pagination.page(1)

                val audience = RecordingAudience()
                RecordingClickCallbackProvider.fire(audience)

                val sent = audience.messages.single()
                sent shouldContainText "Beta"
                sent shouldContainText "2/3"
                sent shouldNotContainText "Alpha"
                sent shouldNotContainText "Gamma"
            }

            "prev button sends the preceding page to the clicking audience" {
                RecordingClickCallbackProvider.reset()
                val pagination =
                    paginate(listOf("Alpha", "Beta", "Gamma")) {
                        renderer { text(it) }
                        itemsPerPage(1)
                    }
                pagination.page(3)

                val audience = RecordingAudience()
                RecordingClickCallbackProvider.fire(audience)

                val sent = audience.messages.single()
                sent shouldContainText "Beta"
                sent shouldContainText "2/3"
            }

            "applies custom nav labels" {
                val pagination =
                    paginate(listOf("Alpha", "Beta", "Gamma")) {
                        renderer { text(it) }
                        itemsPerPage(1)
                        nav {
                            previous { text("Back") }
                            next(text("Forward"))
                        }
                    }

                val page = pagination.page(2)
                page shouldContainText "Back"
                page shouldContainText "Forward"
                page shouldNotContainText "Previous"
                page shouldNotContainText "Next »"
            }

            "applies a custom indicator" {
                val pagination =
                    paginate(listOf("Alpha", "Beta")) {
                        renderer { text(it) }
                        itemsPerPage(1)
                        nav {
                            indicator { page, pageCount -> text("page $page of $pageCount") }
                        }
                    }

                pagination.page(1) shouldContainText "page 1 of 2"
            }

            "hides the indicator" {
                val pagination =
                    paginate(listOf("Alpha", "Beta", "Gamma")) {
                        renderer { text(it) }
                        itemsPerPage(1)
                        nav {
                            indicator(false)
                        }
                    }

                val nav = pagination.page(2).childAt(2)
                nav shouldHaveContent "« Previous Next »"
            }

            "passes uses and lifetime to the callback options" {
                RecordingClickCallbackProvider.reset()
                val pagination =
                    paginate(listOf("Alpha", "Beta")) {
                        renderer { text(it) }
                        itemsPerPage(1)
                        nav {
                            uses(3)
                            lifetime(10.minutes)
                        }
                    }
                pagination.page(1)

                RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 3
                RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe JavaDuration.ofMinutes(10)
            }

            "defaults nav callbacks to unlimited uses within adventure's default lifetime" {
                RecordingClickCallbackProvider.reset()
                val pagination =
                    paginate(listOf("Alpha", "Beta")) {
                        renderer { text(it) }
                        itemsPerPage(1)
                    }
                pagination.page(1)

                RecordingClickCallbackProvider.lastOptions?.uses() shouldBe ClickCallback.UNLIMITED_USES
                RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe ClickCallback.DEFAULT_LIFETIME
            }

            "sends the first page to the audience" {
                val audience = RecordingAudience()

                audience.paginate("Alpha", "Beta", "Gamma") {
                    header { text("Warps") }
                    renderer { text(it) }
                    itemsPerPage(2)
                }

                audience.messages shouldHaveSize 1
                val sent = audience.messages.single()
                sent shouldContainText "Warps"
                sent shouldContainText "Alpha"
                sent shouldContainText "Beta"
                sent shouldContainText "1/2"
                sent shouldNotContainText "Gamma"
            }

            "rejects a missing renderer" {
                shouldThrow<IllegalStateException> {
                    paginate(listOf("Alpha")) {}
                }
            }

            "rejects a second renderer" {
                shouldThrow<IllegalStateException> {
                    paginate(listOf("Alpha")) {
                        renderer { text(it) }
                        renderer { text(it) }
                    }
                }
            }

            "rejects a second header" {
                shouldThrow<IllegalStateException> {
                    paginate(listOf("Alpha")) {
                        renderer { text(it) }
                        header { text("One") }
                        header(text("Two"))
                    }
                }
            }

            "rejects a second itemsPerPage" {
                shouldThrow<IllegalStateException> {
                    paginate(listOf("Alpha")) {
                        renderer { text(it) }
                        itemsPerPage(2)
                        itemsPerPage(3)
                    }
                }
            }

            "rejects a second nav block" {
                shouldThrow<IllegalStateException> {
                    paginate(listOf("Alpha")) {
                        renderer { text(it) }
                        nav {}
                        nav {}
                    }
                }
            }

            "rejects a non-positive itemsPerPage" {
                shouldThrow<IllegalArgumentException> {
                    paginate(listOf("Alpha")) {
                        renderer { text(it) }
                        itemsPerPage(0)
                    }
                }
            }

            "rejects a second previous label" {
                shouldThrow<IllegalStateException> {
                    paginate(listOf("Alpha")) {
                        renderer { text(it) }
                        nav {
                            previous { text("Back") }
                            previous(text("Back again"))
                        }
                    }
                }
            }

            "rejects a second next label" {
                shouldThrow<IllegalStateException> {
                    paginate(listOf("Alpha")) {
                        renderer { text(it) }
                        nav {
                            next { text("Next") }
                            next(text("Next again"))
                        }
                    }
                }
            }

            "rejects a second indicator" {
                shouldThrow<IllegalStateException> {
                    paginate(listOf("Alpha")) {
                        renderer { text(it) }
                        nav {
                            indicator(false)
                            indicator { page, pageCount -> text("$page/$pageCount") }
                        }
                    }
                }
            }

            "rejects a second uses count" {
                shouldThrow<IllegalStateException> {
                    paginate(listOf("Alpha")) {
                        renderer { text(it) }
                        nav {
                            uses(1)
                            uses(2)
                        }
                    }
                }
            }

            "rejects a second lifetime" {
                shouldThrow<IllegalStateException> {
                    paginate(listOf("Alpha")) {
                        renderer { text(it) }
                        nav {
                            lifetime(1.minutes)
                            lifetime(2.minutes)
                        }
                    }
                }
            }

            "rejects invalid uses" {
                shouldThrow<IllegalArgumentException> {
                    paginate(listOf("Alpha")) {
                        renderer { text(it) }
                        nav {
                            uses(0)
                        }
                    }
                }
            }

            "accepts unlimited uses" {
                RecordingClickCallbackProvider.reset()
                val pagination =
                    paginate(listOf("Alpha", "Beta")) {
                        renderer { text(it) }
                        itemsPerPage(1)
                        nav {
                            uses(unlimited)
                        }
                    }
                pagination.page(1)

                RecordingClickCallbackProvider.lastOptions?.uses() shouldBe ClickCallback.UNLIMITED_USES
            }

            "rejects a non-positive lifetime" {
                shouldThrow<IllegalArgumentException> {
                    paginate(listOf("Alpha")) {
                        renderer { text(it) }
                        nav {
                            lifetime(Duration.ZERO)
                        }
                    }
                }
            }

            "rejects a page outside the range" {
                val pagination =
                    paginate(listOf("Alpha", "Beta")) {
                        renderer { text(it) }
                        itemsPerPage(1)
                    }

                shouldThrow<IllegalArgumentException> { pagination.page(0) }
                shouldThrow<IllegalArgumentException> { pagination.page(3) }
            }
        },
    )
