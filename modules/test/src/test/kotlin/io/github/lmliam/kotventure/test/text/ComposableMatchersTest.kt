package io.github.lmliam.kotventure.test.text

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.and
import io.kotest.matchers.or
import io.kotest.matchers.should
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class ComposableMatchersTest :
    StringSpec(
        {
            "composes attribute matchers with and" {
                val component =
                    Component
                        .text("Alert")
                        .color(NamedTextColor.RED)
                        .decoration(TextDecoration.BOLD, true)

                component should (haveColor(NamedTextColor.RED) and haveDecoration(TextDecoration.BOLD))
            }

            "fails a composed and matcher reporting the unmet attribute" {
                val component = Component.text("Alert").color(NamedTextColor.RED)

                val failure =
                    shouldThrow<AssertionError> {
                        component should (haveColor(NamedTextColor.RED) and haveDecoration(TextDecoration.BOLD))
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <TRUE>, but was <NOT_SET>."

                failure.message shouldContain expectedMessage
            }

            "composes attribute matchers with or" {
                val component = Component.text("Alert").color(NamedTextColor.BLUE)

                component should (haveColor(NamedTextColor.RED) or haveColor(NamedTextColor.BLUE))
            }

            "fails a composed or matcher when no branch matches" {
                val component = Component.text("Alert").color(NamedTextColor.GREEN)

                val failure =
                    shouldThrow<AssertionError> {
                        component should (haveColor(NamedTextColor.RED) or haveColor(NamedTextColor.BLUE))
                    }

                failure.message shouldContain "component color"
            }

            "negates a matcher with shouldNot" {
                val component = Component.text("Alert").color(NamedTextColor.RED)

                component shouldNot haveColor(NamedTextColor.BLUE)
            }

            "reports the negated failure message when a negated matcher matches" {
                val component = Component.text("Alert").color(NamedTextColor.RED)

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldNot haveColor(NamedTextColor.RED)
                    }
                val expectedMessage = "Expected component color not to be <${NamedTextColor.RED}>."

                failure.message shouldContain expectedMessage
            }

            "composes matchers across different component attributes" {
                val component =
                    Component
                        .text("Greeting")
                        .color(NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, true)

                component should
                        (
                            containText("Greet") and haveColor(NamedTextColor.AQUA) and
                            haveDecoration(TextDecoration.ITALIC)
                        )
            }

            "inverts a matcher to assert the opposite" {
                val component = Component.text("Alert").color(NamedTextColor.RED)

                component should haveColor(NamedTextColor.BLUE).invert()
            }
        },
    )
