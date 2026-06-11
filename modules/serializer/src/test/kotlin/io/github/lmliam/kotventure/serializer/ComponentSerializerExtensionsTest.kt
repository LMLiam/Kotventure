package io.github.lmliam.kotventure.serializer

import io.github.lmliam.kotventure.core.color.hex
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.objectcomponent.display
import io.github.lmliam.kotventure.core.objectcomponent.sprite
import io.github.lmliam.kotventure.core.text.component
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldBeObjectComponent
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveObjectContents
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage

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

            "serializes hex colors with Adventure MiniMessage formatting" {
                val message =
                    component {
                        text("Brand") {
                            color(hex("#123ABC"))
                        }
                    }

                val serialized = message.toMiniMessage()
                serialized shouldBe "<#123ABC>Brand"

                val roundTripped = MiniMessage.miniMessage().deserialize(serialized)

                roundTripped shouldContainText "Brand"
                roundTripped shouldHaveColor hex("#123ABC")
            }

            "round-trips gradient text formatting through MiniMessage" {
                val message =
                    component {
                        text("ace") {
                            gradient(NamedTextColor.RED, NamedTextColor.GOLD, NamedTextColor.AQUA)
                        }
                    }

                val serialized = message.toMiniMessage()
                serialized shouldBe "<red>a</red><gold>c</gold><aqua>e"

                val roundTripped = MiniMessage.miniMessage().deserialize(serialized)

                roundTripped shouldContainText "ace"
                roundTripped shouldHaveChildCount 3
                roundTripped.childAt(0) shouldHaveColor NamedTextColor.RED
                roundTripped.childAt(1) shouldHaveColor NamedTextColor.GOLD
                roundTripped.childAt(2) shouldHaveColor NamedTextColor.AQUA
            }

            "serializes object components to plain text" {
                val contents = sprite(key("minecraft", "block/stone"))
                val message = display(contents)

                message.toPlainText() shouldBe "[block/stone]"
            }

            "round-trips sprite object components through MiniMessage" {
                val contents = sprite(key("minecraft", "block/stone"))
                val message = display(contents)

                val serialized = message.toMiniMessage()
                serialized shouldBe "<sprite:'minecraft:block/stone'>"

                val roundTripped = MiniMessage.miniMessage().deserialize(serialized).shouldBeObjectComponent()

                roundTripped shouldHaveObjectContents contents
            }
        },
    )
