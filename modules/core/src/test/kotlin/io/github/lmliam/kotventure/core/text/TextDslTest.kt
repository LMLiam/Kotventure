package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class TextDslTest :
    StringSpec(
        {
            "builds a text component from a value" {
                val component = text("Title")

                component shouldContainText "Title"
                component shouldHaveChildCount 0
            }

            "builds a text component from a value and a configuration block" {
                val component =
                    text("Title") {
                        color(NamedTextColor.AQUA)
                        bold()
                    }

                component shouldContainText "Title"
                component shouldHaveColor NamedTextColor.AQUA
                component shouldHaveDecoration TextDecoration.BOLD
            }

            "builds a text component from a configuration block alone" {
                val component =
                    text {
                        content("Title")
                        color(NamedTextColor.GOLD)
                    }

                component shouldContainText "Title"
                component shouldHaveColor NamedTextColor.GOLD
            }

            "rejects setting content twice in one block" {
                shouldThrow<IllegalStateException> {
                    text {
                        content("Title")
                        content("Subtitle")
                    }
                }
            }

            "rejects setting content when a literal value is already supplied" {
                shouldThrow<IllegalStateException> {
                    text("Title") {
                        content("Subtitle")
                    }
                }
            }
        },
    )
