package io.github.lmliam.kotventure.core.text

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

class ComponentDslTest :
    StringSpec(
        {
            "builds a text component with content" {
                val component =
                    component {
                        content("Hello")
                    }

                component as TextComponent
                component.content() shouldBe "Hello"
            }

            "applies color to the root text component" {
                val component =
                    component {
                        content("Warning")
                        color(NamedTextColor.RED)
                    }

                component.color() shouldBe NamedTextColor.RED
            }

            "appends nested text children in declaration order" {
                val component =
                    component {
                        content("Hello ")
                        text {
                            content("world")
                            color(NamedTextColor.AQUA)
                        }
                        text {
                            content("!")
                        }
                    }

                component.children() shouldHaveSize 2
                val firstChild = component.children()[0] as TextComponent
                val secondChild = component.children()[1] as TextComponent
                firstChild.content() shouldBe "world"
                firstChild.color() shouldBe NamedTextColor.AQUA
                secondChild.content() shouldBe "!"
            }

            "applies a complete Adventure style" {
                val style = Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)

                val component =
                    component {
                        content("Title")
                        style(style)
                    }

                component.style() shouldBe style
            }
        },
    )
