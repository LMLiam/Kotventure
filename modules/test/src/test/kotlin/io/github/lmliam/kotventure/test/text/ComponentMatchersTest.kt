package io.github.lmliam.kotventure.test.text

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

class ComponentMatchersTest :
    StringSpec(
        {
            "matches text content on Adventure text components" {
                Component.text("Hello") shouldContainText "Hello"
            }

            "matches text content nested in child components" {
                val component =
                    Component
                        .text()
                        .content("Hello ")
                        .append(Component.text("world"))
                        .build()

                component shouldContainText "world"
            }

            "reports text mismatch with expected and actual content" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Hello") shouldContainText "Bye"
                    }
                val expectedMessage = "Expected component text to contain <Bye>, but was <Hello>."

                failure.message shouldContain expectedMessage
            }

            "reports nested text mismatch with the complete extracted content" {
                val component =
                    Component
                        .text()
                        .content("Hello ")
                        .append(Component.text("world"))
                        .append(Component.text("!"))
                        .build()

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldContainText "missing"
                    }
                val expectedMessage =
                    "Expected component text to contain <missing>, but was <Hello world!>."

                failure.message shouldContain expectedMessage
            }

            "matches root component colors" {
                Component.text("Warning", NamedTextColor.RED) shouldHaveColor NamedTextColor.RED
            }

            "reports color mismatch with expected and actual colors" {
                val component = Component.text("Warning", NamedTextColor.RED)

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldHaveColor NamedTextColor.BLUE
                    }
                val expectedMessage =
                    "Expected component color <${NamedTextColor.BLUE}>, " +
                            "but was <${NamedTextColor.RED}>."

                failure.message shouldContain expectedMessage
            }

            "reports missing root color with expected and actual colors" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Warning") shouldHaveColor NamedTextColor.RED
                    }
                val expectedMessage =
                    "Expected component color <${NamedTextColor.RED}>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "matches complete Adventure styles" {
                val style = Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)

                Component.text("Title").style(style) shouldHaveStyle style
            }

            "matches root decorations" {
                Component
                    .text("Title")
                    .decoration(TextDecoration.BOLD, true) shouldHaveDecoration TextDecoration.BOLD
            }

            "reports decoration mismatch with expected and actual state" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Title") shouldHaveDecoration TextDecoration.BOLD
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <TRUE>, " +
                            "but was <NOT_SET>."

                failure.message shouldContain expectedMessage
            }

            "matches child count and retrieves children by index" {
                val component =
                    Component
                        .text()
                        .content("Hello ")
                        .append(Component.text("world"))
                        .build()

                component shouldHaveChildCount 1
                component.childAt(0) shouldContainText "world"
            }

            "reports missing child indexes clearly" {
                val failure =
                    shouldThrow<IllegalStateException> {
                        Component.text("Hello").childAt(0)
                    }
                val expectedMessage = "Expected child at index <0>, but component has <0> children."

                failure.message shouldContain expectedMessage
            }
        },
    )
