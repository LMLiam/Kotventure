package io.github.lmliam.kotventure.test.text

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.`object`.ObjectContents

class ObjectComponentMatchersTest :
    StringSpec(
        {
            "matches object component contents and fallback" {
                val contents = ObjectContents.sprite(Key.key("minecraft", "block/stone"))
                val fallback = Component.text("[stone]")
                val component =
                    Component
                        .`object`()
                        .contents(contents)
                        .fallback(fallback)
                        .build()
                        .shouldBeObjectComponent()

                component shouldHaveObjectContents contents
                component shouldHaveObjectFallback fallback
            }

            "matches object components without fallback" {
                val contents = ObjectContents.sprite(Key.key("minecraft", "block/stone"))

                Component.`object`(contents).shouldBeObjectComponent().shouldNotHaveObjectFallback()
            }

            "reports non-object components before object assertions" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component.text("plain").shouldBeObjectComponent()
                    }
                val expectedMessage = "Expected object component, but was <TextComponentImpl>."

                failure.message shouldContain expectedMessage
            }

            "reports object contents mismatches" {
                val actual = ObjectContents.sprite(Key.key("minecraft", "block/stone"))
                val expected = ObjectContents.sprite(Key.key("minecraft", "block/dirt"))

                val failure =
                    shouldThrow<AssertionError> {
                        Component.`object`(actual).shouldBeObjectComponent() shouldHaveObjectContents expected
                    }
                val expectedMessage = "Expected object contents <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "reports object fallback mismatches" {
                val contents = ObjectContents.sprite(Key.key("minecraft", "block/stone"))
                val actual = Component.text("[stone]")
                val expected = Component.text("[dirt]")

                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .`object`()
                            .contents(contents)
                            .fallback(actual)
                            .build()
                            .shouldBeObjectComponent() shouldHaveObjectFallback expected
                    }
                val expectedMessage = "Expected object fallback <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "reports missing object fallback" {
                val contents = ObjectContents.sprite(Key.key("minecraft", "block/stone"))
                val expected = Component.text("[stone]")

                val failure =
                    shouldThrow<AssertionError> {
                        Component.`object`(contents).shouldBeObjectComponent() shouldHaveObjectFallback expected
                    }
                val expectedMessage = "Expected object fallback <$expected>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "reports unexpected object fallback" {
                val contents = ObjectContents.sprite(Key.key("minecraft", "block/stone"))
                val fallback = Component.text("[stone]")

                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .`object`()
                            .contents(contents)
                            .fallback(fallback)
                            .build()
                            .shouldBeObjectComponent()
                            .shouldNotHaveObjectFallback()
                    }
                val expectedMessage = "Expected object fallback to be absent, but was <$fallback>."

                failure.message shouldContain expectedMessage
            }
        },
    )
