package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.text.text
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component

class KeybindMatchersTest :
    StringSpec(
        {
            "matches keybind component keys" {
                Component
                    .keybind("key.jump")
                    .shouldBeKeybindComponent() shouldHaveKeybind "key.jump"
            }

            "reports keybind mismatch with expected and actual keys" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .keybind("key.jump")
                            .shouldBeKeybindComponent() shouldHaveKeybind "key.sneak"
                    }
                val expectedMessage = "Expected keybind <key.sneak>, but was <key.jump>."

                failure.message shouldContain expectedMessage
            }

            "reports non-keybind components before keybind assertions" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("plain").shouldBeKeybindComponent()
                    }
                val expectedMessage = "Expected keybind component, but was <TextComponentImpl>."

                failure.message shouldContain expectedMessage
            }
        },
    )
