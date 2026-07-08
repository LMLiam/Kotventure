package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.color.blue
import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.text.text
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
                text("Warning") { color(red) } shouldHaveColor red
            }

            "reports color mismatch with expected and actual colors" {
                val component = text("Warning") { color(red) }

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldHaveColor blue
                    }
                val expectedMessage =
                    "Expected component color <$blue>, " +
                            "but was <$red>."

                failure.message shouldContain expectedMessage
            }

            "reports missing root color with expected and actual colors" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("Warning") shouldHaveColor red
                    }
                val expectedMessage =
                    "Expected component color <$red>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "matches the absence of a root color" {
                text("Plain").shouldNotHaveColor()
            }

            "reports unexpected root colors" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("Warning") { color(red) }.shouldNotHaveColor()
                    }
                val expectedMessage =
                    "Expected component color to be absent, but was <$red>."

                failure.message shouldContain expectedMessage
            }

            "matches root shadow colors" {
                val shadow = ShadowColor.shadowColor(0, 0, 0, 255)

                text("Glow").shadowColor(shadow) shouldHaveShadowColor shadow
            }

            "reports shadow color mismatch with expected and actual colors" {
                val actual = ShadowColor.shadowColor(0, 0, 0, 255)
                val expected = ShadowColor.shadowColor(255, 255, 255, 255)

                val failure =
                    shouldThrow<AssertionError> {
                        text("Glow").shadowColor(actual) shouldHaveShadowColor expected
                    }
                val expectedMessage = "Expected component shadow color <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "matches the absence of a root shadow color" {
                text("Plain").shouldNotHaveShadowColor()
            }

            "reports unexpected root shadow colors" {
                val shadow = ShadowColor.shadowColor(0, 0, 0, 255)

                val failure =
                    shouldThrow<AssertionError> {
                        text("Glow").shadowColor(shadow).shouldNotHaveShadowColor()
                    }
                val expectedMessage = "Expected component shadow color to be absent, but was <$shadow>."

                failure.message shouldContain expectedMessage
            }
        },
    )
