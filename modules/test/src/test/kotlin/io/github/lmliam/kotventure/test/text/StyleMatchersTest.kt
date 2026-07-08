package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.style.style
import io.github.lmliam.kotventure.core.text.text
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration

class StyleMatchersTest :
    StringSpec(
        {
            "matches complete Adventure styles" {
                val style =
                    style {
                        color(gold)
                        bold()
                    }

                text("Title").style(style) shouldHaveStyle style
            }

            "reports style mismatch with expected and actual styles" {
                val actual = style { color(gold) }
                val expected = style { color(aqua) }

                val failure =
                    shouldThrow<AssertionError> {
                        text("Title").style(actual) shouldHaveStyle expected
                    }
                val expectedMessage = "Expected component style <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "matches root component fonts" {
                val font = key("minecraft", "uniform")

                text("Title").font(font) shouldHaveFont font
            }

            "reports font mismatch with expected and actual fonts" {
                val actual = key("minecraft", "uniform")
                val expected = key("minecraft", "alt")

                val failure =
                    shouldThrow<AssertionError> {
                        text("Title").font(actual) shouldHaveFont expected
                    }
                val expectedMessage = "Expected component font <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "reports a missing font as null when asserting presence" {
                val expected = key("minecraft", "uniform")

                val failure =
                    shouldThrow<AssertionError> {
                        text("Title") shouldHaveFont expected
                    }
                val expectedMessage = "Expected component font <$expected>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "matches the absence of a root font" {
                text("Title").shouldNotHaveFont()
            }

            "reports unexpected root fonts" {
                val font = key("minecraft", "uniform")

                val failure =
                    shouldThrow<AssertionError> {
                        text("Title").font(font).shouldNotHaveFont()
                    }
                val expectedMessage = "Expected component font to be absent, but was <$font>."

                failure.message shouldContain expectedMessage
            }

            "matches root insertion text" {
                text("Name").insertion("Steve") shouldHaveInsertion "Steve"
            }

            "reports insertion mismatch with expected and actual text" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("Name").insertion("Steve") shouldHaveInsertion "Alex"
                    }
                val expectedMessage = "Expected component insertion <Alex>, but was <Steve>."

                failure.message shouldContain expectedMessage
            }

            "reports a missing insertion as null when asserting presence" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("Name") shouldHaveInsertion "Steve"
                    }
                val expectedMessage = "Expected component insertion <Steve>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "matches the absence of root insertion text" {
                text("Name").shouldNotHaveInsertion()
            }

            "reports unexpected root insertion text" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("Name").insertion("Steve").shouldNotHaveInsertion()
                    }
                val expectedMessage = "Expected component insertion to be absent, but was <Steve>."

                failure.message shouldContain expectedMessage
            }
        },
    )
