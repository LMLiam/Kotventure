package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component

// Build the string-literal operators outside the StringSpec lambda.
// The Kotest `String.invoke` operator would otherwise hide the `"text" { }` operation.
// This name conflict occurs only in the test harness.
private fun styledChild(): Component =
    component {
        "Hello" {
            color(gold)
        }
    }

private fun plainChild(): Component =
    component {
        +"Hello"
    }

private fun nestedChildren(): Component =
    component {
        "outer" {
            +"inner"
        }
    }

private fun siblingChildren(): Component =
    component {
        +"first"
        "second" {
            color(gold)
        }
    }

private fun styledViaSugar(): Component = component { "Hello" { color(gold) } }

private fun styledViaText(): Component = component { text("Hello") { color(gold) } }

class StringLiteralTextDslTest :
    StringSpec(
        {
            "invoke appends a styled text child" {
                val message = styledChild()

                message shouldHaveChildCount 1
                message.childAt(0) shouldContainText "Hello"
                message.childAt(0) shouldHaveColor gold
            }

            "unary plus appends a plain text child" {
                val message = plainChild()

                message shouldHaveChildCount 1
                message.childAt(0) shouldContainText "Hello"
            }

            "invoke nests further children under the styled child's scope" {
                val message = nestedChildren()

                message shouldHaveChildCount 1
                val outer = message.childAt(0)
                outer shouldContainText "outer"
                outer shouldHaveChildCount 1
                outer.childAt(0) shouldContainText "inner"
            }

            "the sugar appends siblings in declaration order" {
                val message = siblingChildren()

                message shouldHaveChildCount 2
                message.childAt(0) shouldContainText "first"
                message.childAt(1) shouldContainText "second"
                message.childAt(1) shouldHaveColor gold
            }

            "invoke is exactly the text(value) child entry point" {
                styledViaSugar() shouldBe styledViaText()
            }

            "the string-literal sugar is unavailable outside a component scope" {
                assertDoesNotCompile(
                    fileName = "StringSugarScopeTest.kt",
                    source =
                        """
                        import io.github.lmliam.kotventure.core.text.unaryPlus

                        fun shouldNotCompile() {
                            +"orphaned"
                        }
                        """.trimIndent(),
                    "No context argument for 'scope: ComponentScope' found.",
                )
            }
        },
    )
