package io.github.lmliam.kotventure.core.color

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
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
            "constructs gradients only through the gradient factories" {
                assertDoesNotCompile(
                    "ColorGradientConstructorVisibilityTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.color.ColorGradient
                    import net.kyori.adventure.text.format.NamedTextColor

                    fun invalid() {
                        ColorGradient(listOf(NamedTextColor.RED, NamedTextColor.BLUE))
                    }
                    """.trimIndent(),
                    "Cannot access",
                )
            }

            "renders a multi stop gradient as one styled child per code point" {
                val message = gradientText("abcde", red, gold, aqua)

                message shouldContainText "abcde"
                message shouldHaveChildCount 5
                message.childAt(0) shouldContainText "a"
                message.childAt(0) shouldHaveColor red
                message.childAt(1) shouldContainText "b"
                message.childAt(1) shouldHaveColor interpolate(0.5f, red, gold)
                message.childAt(2) shouldContainText "c"
                message.childAt(2) shouldHaveColor gold
                message.childAt(3) shouldContainText "d"
                message.childAt(3) shouldHaveColor interpolate(0.5f, gold, aqua)
                message.childAt(4) shouldContainText "e"
                message.childAt(4) shouldHaveColor aqua
            }

            "handles empty single character and supplementary code point text deterministically" {
                val empty = gradientText("", red, blue)
                val single = gradientText("x", red, blue)
                val symbol = "\uD834\uDD1E"
                val mixed = gradientText("A${symbol}B", red, gold)

                empty shouldHaveChildCount 0

                single shouldHaveChildCount 1
                single.childAt(0) shouldContainText "x"
                single.childAt(0) shouldHaveColor red

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
                            gradient(red, gold, aqua)
                        }
                        text("<")
                    }

                message shouldContainText ">abc<"
                message shouldHaveChildCount 3
                message.childAt(0) shouldContainText ">"
                message.childAt(1) shouldContainText "abc"
                message.childAt(1) shouldHaveChildCount 3
                message.childAt(1).childAt(0) shouldContainText "a"
                message.childAt(1).childAt(0) shouldHaveColor red
                message.childAt(1).childAt(1) shouldContainText "b"
                message.childAt(1).childAt(1) shouldHaveColor gold
                message.childAt(1).childAt(2) shouldContainText "c"
                message.childAt(1).childAt(2) shouldHaveColor aqua
                message.childAt(2) shouldContainText "<"
            }

            "rejects a gradient on empty text content" {
                shouldThrow<IllegalStateException> {
                    text {
                        gradient(red, blue)
                    }
                }
            }

            "rejects a second gradient in one text block" {
                shouldThrow<IllegalStateException> {
                    text("abc") {
                        gradient(red, blue)
                        gradient(gold, aqua)
                    }
                }
            }

            "rejects gradients with fewer than two stops" {
                shouldThrow<IllegalArgumentException> {
                    gradient(red)
                }

                shouldThrow<IllegalArgumentException> {
                    gradient(emptyList())
                }
            }

            "stores gradient stops immutably" {
                val stops = mutableListOf(red, blue)
                val gradient = gradient(stops)

                stops[0] = green

                gradient.stops shouldBe listOf(red, blue)
            }
        },
    )
