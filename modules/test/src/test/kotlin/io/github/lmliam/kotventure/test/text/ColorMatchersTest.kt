package io.github.lmliam.kotventure.test.text

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor

class ColorMatchersTest :
    StringSpec(
        {
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

            "matches the absence of a root color" {
                Component.text("Plain").shouldNotHaveColor()
            }

            "reports unexpected root colors" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Warning", NamedTextColor.RED).shouldNotHaveColor()
                    }
                val expectedMessage =
                    "Expected component color to be absent, but was <${NamedTextColor.RED}>."

                failure.message shouldContain expectedMessage
            }

            "matches root shadow colors" {
                val shadow = ShadowColor.shadowColor(0, 0, 0, 255)

                Component.text("Glow").shadowColor(shadow) shouldHaveShadowColor shadow
            }

            "reports shadow color mismatch with expected and actual colors" {
                val actual = ShadowColor.shadowColor(0, 0, 0, 255)
                val expected = ShadowColor.shadowColor(255, 255, 255, 255)

                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Glow").shadowColor(actual) shouldHaveShadowColor expected
                    }
                val expectedMessage = "Expected component shadow color <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "matches the absence of a root shadow color" {
                Component.text("Plain").shouldNotHaveShadowColor()
            }

            "reports unexpected root shadow colors" {
                val shadow = ShadowColor.shadowColor(0, 0, 0, 255)

                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Glow").shadowColor(shadow).shouldNotHaveShadowColor()
                    }
                val expectedMessage = "Expected component shadow color to be absent, but was <$shadow>."

                failure.message shouldContain expectedMessage
            }
        },
    )
