package io.github.lmliam.kotventure.serializer

import io.github.lmliam.kotventure.core.text.component
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage

class ComponentSerializerExtensionsTest :
    StringSpec(
        {
            "serializes component text to plain text" {
                val message =
                    component {
                        text("Hello ") {
                            color(NamedTextColor.AQUA)
                        }
                        text("world") {
                            color(NamedTextColor.GOLD)
                        }
                    }

                message.toPlainText() shouldBe "Hello world"
            }

            "serializes unstyled component text to MiniMessage text" {
                val message =
                    component {
                        content("Hello")
                    }

                message.toMiniMessage() shouldBe "Hello"
            }

            "round-trips MiniMessage output through Adventure" {
                val message =
                    component {
                        text("Alert") {
                            color(NamedTextColor.RED)
                        }
                    }

                val serialized = message.toMiniMessage()
                serialized shouldBe "<red>Alert"

                val roundTripped = MiniMessage.miniMessage().deserialize(serialized)

                roundTripped shouldContainText "Alert"
                roundTripped shouldHaveColor NamedTextColor.RED
            }
        },
    )
