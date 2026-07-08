package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.text.text
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component

class ChildMatchersTest :
    StringSpec(
        {
            "matches child count and retrieves children by index" {
                val component =
                    text("Hello ") {
                        text("world")
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldContainText "world"
            }

            "reports child count mismatch with expected and actual counts" {
                val component =
                    text("Hello ") {
                        text("world")
                    }

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldHaveChildCount 2
                    }
                val expectedMessage = "Expected <2> child components, but found <1>."

                failure.message shouldContain expectedMessage
            }

            "matches the absence of children" {
                text("Hello").shouldHaveNoChildren()
            }

            "reports unexpected children" {
                val component =
                    text("Hello ") {
                        text("world")
                    }

                val failure =
                    shouldThrow<AssertionError> {
                        component.shouldHaveNoChildren()
                    }
                val expectedMessage = "Expected <0> child components, but found <1>."

                failure.message shouldContain expectedMessage
            }

            "reports missing child indexes clearly" {
                val failure =
                    shouldThrow<IllegalStateException> {
                        text("Hello").childAt(0)
                    }
                val expectedMessage = "Expected child at index <0>, but component has <0> children."

                failure.message shouldContain expectedMessage
            }

            "matches direct children in order" {
                val first = text("one")
                val second = text("two")
                val component =
                    component {
                        append(first)
                        append(second)
                    }

                component.shouldHaveChildren(first, second)
            }

            "reports children that differ in order" {
                val first = text("one")
                val second = text("two")
                val component =
                    component {
                        append(first)
                        append(second)
                    }

                val failure =
                    shouldThrow<AssertionError> {
                        component.shouldHaveChildren(second, first)
                    }
                val expectedMessage =
                    "Expected children <${listOf(second, first)}>, but was <${listOf(first, second)}>."

                failure.message shouldContain expectedMessage
            }

            "reports children that differ in count" {
                val first = text("one")
                val second = text("two")
                val component = component { append(first) }

                val failure =
                    shouldThrow<AssertionError> {
                        component.shouldHaveChildren(first, second)
                    }
                val expectedMessage =
                    "Expected children <${listOf(first, second)}>, but was <${listOf(first)}>."

                failure.message shouldContain expectedMessage
            }

            "matches a component contained anywhere in the tree" {
                val needle = text("world")
                val component =
                    text("Hello ") {
                        append(needle)
                    }

                component shouldContainComponent needle
            }

            "reports a component missing from the tree" {
                val needle = text("absent")
                val component =
                    text("Hello ") {
                        text("world")
                    }

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldContainComponent needle
                    }
                val expectedMessage = "Expected component tree to contain $needle"

                failure.message shouldContain expectedMessage
            }

            "matches the absence of a component from the tree" {
                val needle = text("absent")
                val component =
                    text("Hello ") {
                        text("world")
                    }

                component shouldNotContainComponent needle
            }

            "reports a component unexpectedly present in the tree" {
                val needle = text("world")
                val component =
                    text("Hello ") {
                        append(needle)
                    }

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldNotContainComponent needle
                    }
                val expectedMessage = "Expected component tree not to contain $needle"

                failure.message shouldContain expectedMessage
            }
        },
    )
