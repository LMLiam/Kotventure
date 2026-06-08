package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveStyle
import io.kotest.core.spec.style.StringSpec
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

                component shouldContainText "Hello"
            }

            "applies color to the root text component" {
                val component =
                    component {
                        content("Warning")
                        color(NamedTextColor.RED)
                    }

                component shouldHaveColor NamedTextColor.RED
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

                component shouldHaveChildCount 2
                component.childAt(0) shouldContainText "world"
                component.childAt(0) shouldHaveColor NamedTextColor.AQUA
                component.childAt(1) shouldContainText "!"
            }

            "applies a complete Adventure style" {
                val style = Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)

                val component =
                    component {
                        content("Title")
                        style(style)
                    }

                component shouldHaveStyle style
            }
        },
    )
