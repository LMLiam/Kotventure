package io.github.lmliam.kotventure.test.text

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component

class SelectorMatchersTest :
    StringSpec(
        {
            "matches selector patterns and separators" {
                val separator = Component.text(", ")
                val component =
                    Component
                        .selector("@a", separator)
                        .shouldBeSelectorComponent()

                component shouldHaveSelectorPattern "@a"
                component shouldHaveSelectorSeparator separator
            }

            "matches selectors without separators" {
                Component.selector("@p").shouldBeSelectorComponent().shouldNotHaveSelectorSeparator()
            }

            "reports non-selector components before separator assertions" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("plain").shouldBeSelectorComponent()
                    }
                val expectedMessage = "Expected selector component, but was <TextComponentImpl>."

                failure.message shouldContain expectedMessage
            }

            "reports selector pattern mismatch with expected and actual patterns" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .selector("@p")
                            .shouldBeSelectorComponent() shouldHaveSelectorPattern "@a"
                    }
                val expectedMessage = "Expected selector pattern <@a>, but was <@p>."

                failure.message shouldContain expectedMessage
            }

            "reports selector separator mismatch with expected and actual separators" {
                val expected = Component.text(", ")
                val actual = Component.text(" | ")
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .selector("@a", actual)
                            .shouldBeSelectorComponent() shouldHaveSelectorSeparator expected
                    }
                val expectedMessage = "Expected selector separator <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "reports missing selector separators" {
                val expected = Component.text(", ")
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .selector("@a")
                            .shouldBeSelectorComponent() shouldHaveSelectorSeparator expected
                    }
                val expectedMessage = "Expected selector separator <$expected>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "reports unexpected selector separators" {
                val actual = Component.text(", ")
                val failure =
                    shouldThrow<AssertionError> {
                        Component.selector("@a", actual).shouldBeSelectorComponent().shouldNotHaveSelectorSeparator()
                    }
                val expectedMessage = "Expected selector separator to be absent, but was <$actual>."

                failure.message shouldContain expectedMessage
            }
        },
    )
