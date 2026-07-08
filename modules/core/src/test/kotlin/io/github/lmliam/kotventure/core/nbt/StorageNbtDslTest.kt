package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.gray
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldBeStorageNbtComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.github.lmliam.kotventure.test.text.shouldHaveNbtPath
import io.github.lmliam.kotventure.test.text.shouldHaveNbtSeparator
import io.github.lmliam.kotventure.test.text.shouldHaveStorageKey
import io.github.lmliam.kotventure.test.text.shouldInterpret
import io.github.lmliam.kotventure.test.text.shouldNotHaveNbtSeparator
import io.github.lmliam.kotventure.test.text.shouldNotInterpret
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration

class StorageNbtDslTest :
    StringSpec(
        {
            "builds a storage nbt component with a key and path" {
                val storage = key("kotventure", "messages")
                val path = nbtPath("welcome")["title"]

                val component = storageNbt(storage, path).shouldBeStorageNbtComponent()

                component shouldHaveStorageKey storage
                component shouldHaveNbtPath "welcome.title"
                component.shouldNotInterpret()
                component.shouldNotHaveNbtSeparator()
            }

            "accepts an nbt path from the string escape hatch" {
                val component =
                    storageNbt(
                        key("kotventure", "messages"),
                        nbtPath("welcome.title"),
                    ).shouldBeStorageNbtComponent()

                component shouldHaveNbtPath "welcome.title"
            }

            "applies style to the storage nbt root" {
                val component =
                    storageNbt(key("kotventure", "messages"), nbtPath("welcome.title")) {
                        color(aqua)
                        bold()
                        style {
                            underlined()
                        }
                    }

                component shouldHaveColor aqua
                component shouldHaveDecoration TextDecoration.BOLD
                component shouldHaveDecoration TextDecoration.UNDERLINED
            }

            "appends child components" {
                val suffix = text(" storage")

                val component =
                    storageNbt(key("kotventure", "messages"), nbtPath("welcome.title")) {
                        append(suffix)
                    }

                component shouldHaveChildCount 1
                component.childAt(0) shouldBe suffix
            }

            "sets interpret true" {
                val component =
                    storageNbt(key("kotventure", "messages"), nbtPath("welcome.title")) {
                        interpret(true)
                    }

                component.shouldBeStorageNbtComponent().shouldInterpret()
            }

            "sets a component separator" {
                val separator = text(", ")
                val path = nbtPath("entries")[all]["id"]

                val component =
                    storageNbt(key("kotventure", "messages"), path) {
                        separator(separator)
                    }

                component.shouldBeStorageNbtComponent() shouldHaveNbtSeparator separator
            }

            "sets an inline text separator" {
                val component =
                    storageNbt(key("kotventure", "messages"), nbtPath("entries[].id")) {
                        separator {
                            content(" | ")
                            color(gray)
                        }
                    }

                val separator = checkNotNull(component.shouldBeStorageNbtComponent().separator())

                separator shouldHaveColor gray
                separator shouldContainText " | "
            }
        },
    )
