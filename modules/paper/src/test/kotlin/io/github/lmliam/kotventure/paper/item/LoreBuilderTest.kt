package io.github.lmliam.kotventure.paper.item

import io.github.lmliam.kotventure.core.color.gray
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.shouldBeItalic
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveContent
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldNotBeItalic
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.format.TextDecoration.State

class LoreBuilderTest :
    StringSpec(
        {
            "adds a plain line with an explicit non-italic default" {
                val lines = lore { +"A" }

                lines shouldHaveSize 1
                lines.single() shouldHaveContent "A"
                lines.single().shouldHaveDecoration(TextDecoration.ITALIC, State.FALSE)
                lines.single().shouldNotBeItalic()
            }

            "adds a styled line with an explicit non-italic default" {
                val lines = lore { "B" { color(gray) } }

                lines shouldHaveSize 1
                lines.single() shouldHaveContent "B"
                lines.single() shouldHaveColor NamedTextColor.GRAY
                lines.single().shouldHaveDecoration(TextDecoration.ITALIC, State.FALSE)
                lines.single().shouldNotBeItalic()
            }

            "preserves an explicit italic decoration" {
                val lines = lore { "B" { italic() } }

                lines.single().shouldBeItalic()
            }

            "adds a prebuilt component as a line" {
                val prebuilt = text("prebuilt")

                val lines = lore { +prebuilt }

                lines.single() shouldHaveContent "prebuilt"
                lines.single().shouldHaveDecoration(TextDecoration.ITALIC, State.FALSE)
            }

            "adds an empty spacer line" {
                val lines = lore { blank() }

                lines.single() shouldBe Component.empty()
            }

            "accumulates lines in call order" {
                val prebuilt = text("third")

                val lines =
                    lore {
                        +"first"
                        "second" { color(gray) }
                        +prebuilt
                        blank()
                    }

                lines shouldContainExactly
                    listOf(
                        Component.text("first").decoration(TextDecoration.ITALIC, State.FALSE),
                        Component
                            .text("second")
                            .color(NamedTextColor.GRAY)
                            .decoration(TextDecoration.ITALIC, State.FALSE),
                        Component.text("third").decoration(TextDecoration.ITALIC, State.FALSE),
                        Component.empty(),
                    )
            }
        },
    )

private fun lore(init: LoreScope.() -> Unit): List<Component> = LoreBuilder().apply(init).build()
