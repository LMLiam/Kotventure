package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.text.text
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.BlockNBTComponent
import net.kyori.adventure.text.Component

class NbtMatchersTest :
    StringSpec(
        {
            val pos = BlockNBTComponent.LocalPos.localPos(1.0, 2.0, 3.0)

            "matches block nbt path, position and type" {
                val component = Component.blockNBT("Items", pos).shouldBeBlockNbtComponent()

                component shouldHaveNbtPath "Items"
                component shouldHaveBlockPos pos
            }

            "reports nbt path mismatch with expected and actual paths" {
                val component = Component.blockNBT("Items", pos).shouldBeBlockNbtComponent()

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldHaveNbtPath "Air"
                    }
                val expectedMessage = "Expected NBT path <Air>, but was <Items>."

                failure.message shouldContain expectedMessage
            }

            "reports block position mismatch with expected and actual positions" {
                val other = BlockNBTComponent.LocalPos.localPos(4.0, 5.0, 6.0)
                val component = Component.blockNBT("Items", pos).shouldBeBlockNbtComponent()

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldHaveBlockPos other
                    }
                val expectedMessage =
                    "Expected block NBT position <${other.asString()}>, but was <${pos.asString()}>."

                failure.message shouldContain expectedMessage
            }

            "matches entity nbt selectors and type" {
                val component = Component.entityNBT("Health", "@a").shouldBeEntityNbtComponent()

                component shouldHaveEntitySelector "@a"
                component shouldHaveNbtPath "Health"
            }

            "reports entity selector mismatch with expected and actual selectors" {
                val component = Component.entityNBT("Health", "@a").shouldBeEntityNbtComponent()

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldHaveEntitySelector "@p"
                    }
                val expectedMessage = "Expected entity NBT selector <@p>, but was <@a>."

                failure.message shouldContain expectedMessage
            }

            "matches storage nbt keys and type" {
                val storage = key("kotventure", "messages")
                val component = Component.storageNBT("welcome.title", storage).shouldBeStorageNbtComponent()

                component shouldHaveStorageKey storage
                component shouldHaveNbtPath "welcome.title"
            }

            "reports storage key mismatch with expected and actual keys" {
                val actual = key("kotventure", "messages")
                val expected = key("kotventure", "errors")
                val component = Component.storageNBT("welcome.title", actual).shouldBeStorageNbtComponent()

                val failure =
                    shouldThrow<AssertionError> {
                        component shouldHaveStorageKey expected
                    }
                val expectedMessage = "Expected storage NBT key <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "matches the interpret flag" {
                Component
                    .blockNBT()
                    .nbtPath("Items")
                    .pos(pos)
                    .interpret(true)
                    .build()
                    .shouldInterpret()
                Component
                    .blockNBT()
                    .nbtPath("Items")
                    .pos(pos)
                    .interpret(false)
                    .build()
                    .shouldNotInterpret()
            }

            "reports an unexpected interpret flag" {
                val component =
                    Component
                        .blockNBT()
                        .nbtPath("Items")
                        .pos(pos)
                        .interpret(false)
                        .build()

                val failure =
                    shouldThrow<AssertionError> {
                        component.shouldInterpret()
                    }
                val expectedMessage = "Expected NBT interpret to be <true>, but was <false>."

                failure.message shouldContain expectedMessage
            }

            "matches nbt separators" {
                val separator = text(", ")
                val component =
                    Component
                        .blockNBT()
                        .nbtPath("Items")
                        .pos(pos)
                        .separator(separator)
                        .build()

                component shouldHaveNbtSeparator separator
            }

            "matches the absence of an nbt separator" {
                Component.blockNBT("Items", pos).shouldNotHaveNbtSeparator()
            }

            "reports an unexpected nbt separator" {
                val separator = text(", ")
                val component =
                    Component
                        .blockNBT()
                        .nbtPath("Items")
                        .pos(pos)
                        .separator(separator)
                        .build()

                val failure =
                    shouldThrow<AssertionError> {
                        component.shouldNotHaveNbtSeparator()
                    }
                val expectedMessage = "Expected NBT separator to be absent, but was <$separator>."

                failure.message shouldContain expectedMessage
            }

            "reports non-block-nbt components before block nbt assertions" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("plain").shouldBeBlockNbtComponent()
                    }
                val expectedMessage = "Expected block NBT component, but was <TextComponentImpl>."

                failure.message shouldContain expectedMessage
            }
        },
    )
