package io.github.lmliam.kotventure.test.text

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component

class ContentMatchersTest :
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

            "matches the absence of text content" {
                Component.text("Hello") shouldNotContainText "Bye"
            }

            "reports unexpectedly present text content" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Hello") shouldNotContainText "Hell"
                    }
                val expectedMessage = "Expected component text not to contain <Hell>."

                failure.message shouldContain expectedMessage
            }

            "matches exact flattened text content" {
                val component =
                    Component
                        .text()
                        .content("Hello ")
                        .append(Component.text("world"))
                        .build()

                component shouldHaveContent "Hello world"
            }

            "reports exact content mismatch with expected and actual content" {
                val component =
                    Component
                        .text()
                        .content("Hello ")
                        .append(Component.text("world"))
                        .build()

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldHaveContent "Hello"
                    }
                val expectedMessage = "Expected component text to be <Hello>, but was <Hello world>."

                failure.message shouldContain expectedMessage
            }

            "matches differing exact content via negation" {
                Component.text("Hello") shouldNotHaveContent "Goodbye"
            }

            "reports exact content that matches when negated" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("Hello") shouldNotHaveContent "Hello"
                    }
                val expectedMessage = "Expected component text not to be <Hello>."

                failure.message shouldContain expectedMessage
            }
        },
    )
