package io.github.lmliam.kotventure.serializer

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.objectcomponent.display
import io.github.lmliam.kotventure.core.text.component
import io.github.lmliam.kotventure.test.text.shouldBeObjectComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveObjectContents
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.`object`.ObjectContents

/**
 * Verifies Kotventure's component serializer extensions delegate to Adventure's concrete serializers.
 */
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
                        text("Hello")
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

            "serializes object components to plain text" {
                val contents = ObjectContents.sprite(key("minecraft", "block/stone"))
                val message = display(contents)

                message.toPlainText() shouldBe "[block/stone]"
            }

            "round-trips sprite object components through MiniMessage" {
                val contents = ObjectContents.sprite(key("minecraft", "block/stone"))
                val message = display(contents)

                val serialized = message.toMiniMessage()
                serialized shouldBe "<sprite:'minecraft:block/stone'>"

                val roundTripped = MiniMessage.miniMessage().deserialize(serialized).shouldBeObjectComponent()

                roundTripped shouldHaveObjectContents contents
            }
        },
    )
