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

                failure.message shouldContain "Expected component text to contain <Bye>, but was <Hello>."
            }

            "matches root component colors" {
                Component.text("Warning", NamedTextColor.RED) shouldHaveColor NamedTextColor.RED
            }

            "matches complete Adventure styles" {
                val style = Style.style(NamedTextColor.GOLD, TextDecoration.BOLD)

                Component.text("Title").style(style) shouldHaveStyle style
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

                failure.message shouldContain "Expected child at index <0>, but component has <0> children."
            }
        },
    )
