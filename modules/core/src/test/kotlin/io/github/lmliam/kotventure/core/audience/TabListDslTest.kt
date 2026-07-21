package io.github.lmliam.kotventure.core.audience

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component

private class TabListRecordingAudience : Audience {
    data class HeaderFooter(
        val header: Component,
        val footer: Component,
    )

    val sent = mutableListOf<HeaderFooter>()

    override fun sendPlayerListHeaderAndFooter(
        header: Component,
        footer: Component,
    ) {
        sent += HeaderFooter(header, footer)
    }
}

class TabListDslTest :
    StringSpec(
        {
            fun record(block: TabListScope.() -> Unit): TabListRecordingAudience.HeaderFooter =
                TabListRecordingAudience().also { it.tabList(block) }.sent.single()

            "builds and sends header and footer via block form" {
                val pair =
                    record {
                        header {
                            text("Welcome") { color(gold) }
                        }
                        footer { text("play.example.org") }
                    }

                pair.header.childAt(0) shouldContainText "Welcome"
                pair.header.childAt(0) shouldHaveColor gold
                pair.footer.childAt(0) shouldContainText "play.example.org"
            }

            "accepts existing components for header and footer" {
                val header = Component.text("Header")
                val footer = Component.text("Footer")

                val pair =
                    record {
                        header(header)
                        footer(footer)
                    }

                pair.header shouldBe header
                pair.footer shouldBe footer
            }

            "sends empty footer when only header is set" {
                val pair = record { header { text("Header only") } }

                pair.header.childAt(0) shouldContainText "Header only"
                pair.footer shouldBe Component.empty()
            }

            "sends empty header when only footer is set" {
                val pair = record { footer { text("Footer only") } }

                pair.header shouldBe Component.empty()
                pair.footer.childAt(0) shouldContainText "Footer only"
            }

            "sends the same header and footer to every member of a forwarding audience" {
                val first = TabListRecordingAudience()
                val second = TabListRecordingAudience()

                audienceOf(first, second).tabList {
                    header { text("Broadcast") }
                    footer { text("Footer") }
                }

                first.sent shouldHaveSize 1
                second.sent shouldHaveSize 1
                first.sent.single() shouldBe second.sent.single()
            }

            listOf(
                "rejects a block with neither header nor footer" to {
                    TabListRecordingAudience().tabList {}
                },
                "rejects a duplicate header" to {
                    TabListRecordingAudience().tabList {
                        header { text("a") }
                        header(Component.text("b"))
                    }
                },
                "rejects a duplicate footer" to {
                    TabListRecordingAudience().tabList {
                        footer { text("a") }
                        footer { text("b") }
                    }
                },
            ).forEach { (name, action) ->
                name {
                    shouldThrow<IllegalStateException> { action() }
                }
            }
        },
    )
