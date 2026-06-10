package io.github.lmliam.kotventure.core.color

import io.github.lmliam.kotventure.core.text.component
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
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

            "appends gradient text directly inside component scopes" {
                val message =
                    component {
                        text(">")
                        gradientText("abc", NamedTextColor.RED, NamedTextColor.GOLD, NamedTextColor.AQUA)
                        gradientText("", NamedTextColor.RED, NamedTextColor.BLUE)
                        text("<")
                    }

                message shouldContainText ">abc<"
                message shouldHaveChildCount 5
                message.childAt(0) shouldContainText ">"
                message.childAt(1) shouldContainText "a"
                message.childAt(1) shouldHaveColor NamedTextColor.RED
                message.childAt(2) shouldContainText "b"
                message.childAt(2) shouldHaveColor NamedTextColor.GOLD
                message.childAt(3) shouldContainText "c"
                message.childAt(3) shouldHaveColor NamedTextColor.AQUA
                message.childAt(4) shouldContainText "<"
            }

            "rejects gradients with fewer than two stops" {
                shouldThrow<IllegalArgumentException> {
                    gradient(NamedTextColor.RED)
                }.message shouldContain "at least 2 stops"

                shouldThrow<IllegalArgumentException> {
                    gradient(emptyList())
                }.message shouldContain "at least 2 stops"
            }

            "stores gradient stops immutably" {
                val stops = mutableListOf(NamedTextColor.RED, NamedTextColor.BLUE)
                val gradient = gradient(stops)

                stops[0] = NamedTextColor.GREEN

                gradient.stops shouldBe listOf(NamedTextColor.RED, NamedTextColor.BLUE)
            }
        },
    )
