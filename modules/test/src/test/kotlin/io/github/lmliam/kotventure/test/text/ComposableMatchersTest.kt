package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.blue
import io.github.lmliam.kotventure.core.color.green
import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.text.text
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
                        .color(red)
                        .decoration(TextDecoration.BOLD, true)

                component should (haveColor(red) and haveDecoration(TextDecoration.BOLD))
            }

            "fails a composed and matcher reporting the unmet attribute" {
                val component = text("Alert").color(red)

                val failure =
                    shouldThrow<AssertionError> {
                        component should (haveColor(red) and haveDecoration(TextDecoration.BOLD))
                    }
                val expectedMessage =
                    "Expected component decoration <${TextDecoration.BOLD}> to be <TRUE>, but was <NOT_SET>."

                failure.message shouldContain expectedMessage
            }

            "composes attribute matchers with or" {
                val component = text("Alert").color(blue)

                component should (haveColor(red) or haveColor(blue))
            }

            "fails a composed or matcher when no branch matches" {
                val component = text("Alert").color(green)

                val failure =
                    shouldThrow<AssertionError> {
                        component should (haveColor(red) or haveColor(blue))
                    }

                failure.message shouldContain "component color"
            }

            "negates a matcher with shouldNot" {
                val component = text("Alert").color(red)

                component shouldNot haveColor(blue)
            }

            "reports the negated failure message when a negated matcher matches" {
                val component = text("Alert").color(red)

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldNot haveColor(red)
                    }
                val expectedMessage = "Expected component color not to be <$red>."

                failure.message shouldContain expectedMessage
            }

            "composes matchers across different component attributes" {
                val component =
                    Component
                        .text("Greeting")
                        .color(aqua)
                        .decoration(TextDecoration.ITALIC, true)

                component should
                        (
                                containText("Greet") and haveColor(aqua) and
                                        haveDecoration(TextDecoration.ITALIC)
                                )
            }

            "inverts a matcher to assert the opposite" {
                val component = text("Alert").color(red)

                component should haveColor(blue).invert()
            }
        },
    )
