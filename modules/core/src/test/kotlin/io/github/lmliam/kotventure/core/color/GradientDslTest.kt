package io.github.lmliam.kotventure.core.color

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.format.NamedTextColor

class GradientDslTest :
    StringSpec(
        {
            "renders a multi stop gradient as one styled child per code point" {
                val message = gradientText("abcde", NamedTextColor.RED, NamedTextColor.GOLD, NamedTextColor.AQUA)

                message shouldContainText "abcde"
                message shouldHaveChildCount 5
                message.childAt(0) shouldContainText "a"
                message.childAt(0) shouldHaveColor NamedTextColor.RED
                message.childAt(1) shouldContainText "b"
                message.childAt(1) shouldHaveColor interpolate(0.5f, NamedTextColor.RED, NamedTextColor.GOLD)
                message.childAt(2) shouldContainText "c"
                message.childAt(2) shouldHaveColor NamedTextColor.GOLD
                message.childAt(3) shouldContainText "d"
                message.childAt(3) shouldHaveColor interpolate(0.5f, NamedTextColor.GOLD, NamedTextColor.AQUA)
                message.childAt(4) shouldContainText "e"
                message.childAt(4) shouldHaveColor NamedTextColor.AQUA
            }

            "handles empty single character and supplementary code point text deterministically" {
                val empty = gradientText("", NamedTextColor.RED, NamedTextColor.BLUE)
                val single = gradientText("x", NamedTextColor.RED, NamedTextColor.BLUE)
                val symbol = "\uD834\uDD1E"
                val mixed = gradientText("A${symbol}B", NamedTextColor.RED, NamedTextColor.GOLD)

                empty shouldHaveChildCount 0

                single shouldHaveChildCount 1
                single.childAt(0) shouldContainText "x"
                single.childAt(0) shouldHaveColor NamedTextColor.RED

                mixed shouldContainText "A${symbol}B"
                mixed shouldHaveChildCount 3
                mixed.childAt(0) shouldContainText "A"
                mixed.childAt(1) shouldContainText symbol
                mixed.childAt(2) shouldContainText "B"
            }

            "applies gradient text inside text scopes" {
                val message =
                    component {
                        text(">")
                        text("abc") {
                            gradient(NamedTextColor.RED, NamedTextColor.GOLD, NamedTextColor.AQUA)
                        }
                        text("<")
                    }

                message shouldContainText ">abc<"
                message shouldHaveChildCount 3
                message.childAt(0) shouldContainText ">"
                message.childAt(1) shouldContainText "abc"
                message.childAt(1) shouldHaveChildCount 3
                message.childAt(1).childAt(0) shouldContainText "a"
                message.childAt(1).childAt(0) shouldHaveColor NamedTextColor.RED
                message.childAt(1).childAt(1) shouldContainText "b"
                message.childAt(1).childAt(1) shouldHaveColor NamedTextColor.GOLD
                message.childAt(1).childAt(2) shouldContainText "c"
                message.childAt(1).childAt(2) shouldHaveColor NamedTextColor.AQUA
                message.childAt(2) shouldContainText "<"
            }

            "rejects a gradient on empty text content" {
                shouldThrow<IllegalStateException> {
                    text {
                        gradient(NamedTextColor.RED, NamedTextColor.BLUE)
                    }
                }
            }

            "rejects a second gradient in one text block" {
                shouldThrow<IllegalStateException> {
                    text("abc") {
                        gradient(NamedTextColor.RED, NamedTextColor.BLUE)
                        gradient(NamedTextColor.GOLD, NamedTextColor.AQUA)
                    }
                }
            }

            "rejects gradients with fewer than two stops" {
                shouldThrow<IllegalArgumentException> {
                    gradient(NamedTextColor.RED)
                }

                shouldThrow<IllegalArgumentException> {
                    gradient(emptyList())
                }
            }

            "stores gradient stops immutably" {
                val stops = mutableListOf(NamedTextColor.RED, NamedTextColor.BLUE)
                val gradient = gradient(stops)

                stops[0] = NamedTextColor.GREEN

                gradient.stops shouldBe listOf(NamedTextColor.RED, NamedTextColor.BLUE)
            }
        },
    )
