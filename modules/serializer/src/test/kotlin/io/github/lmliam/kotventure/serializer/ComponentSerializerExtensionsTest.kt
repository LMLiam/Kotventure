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
import io.github.lmliam.kotventure.test.text.shouldHaveClickEvent
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveHoverEntity
import io.github.lmliam.kotventure.test.text.shouldHaveHoverItem
import io.github.lmliam.kotventure.test.text.shouldHaveHoverText
import io.github.lmliam.kotventure.test.text.shouldHaveObjectContents
import io.github.lmliam.kotventure.test.text.shouldNotHaveColor
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.UUID

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
                message.toPlain() shouldBe "Hello world"
            }

            "deserializes plain text strings" {
                val message = "Hello world".plain()

                message shouldContainText "Hello world"
                message.shouldNotHaveColor()
            }

            "round-trips legacy ampersand strings" {
                val message = "&aHello".legacy()

                message shouldContainText "Hello"
                message shouldHaveColor NamedTextColor.GREEN
                message.toLegacy() shouldBe "&aHello"
            }

            "round-trips legacy section strings" {
                val message = "\u00a7bHello".section()

                message shouldContainText "Hello"
                message shouldHaveColor NamedTextColor.AQUA
                message.toSection() shouldBe "\u00a7bHello"
            }

            "round-trips JSON strings with color and events" {
                val message =
                    Component
                        .text("Portal", NamedTextColor.LIGHT_PURPLE)
                        .clickEvent(ClickEvent.runCommand("/spawn"))
                        .hoverEvent(Component.text("Teleport"))

                val json = message.toJson()
                val roundTripped = json.fromJson()

                roundTripped shouldContainText "Portal"
                roundTripped shouldHaveColor NamedTextColor.LIGHT_PURPLE
                roundTripped shouldHaveClickEvent ClickEvent.runCommand("/spawn")
                roundTripped shouldHaveHoverText Component.text("Teleport")
            }

            "serializes unstyled component text to MiniMessage text" {
                val message =
                    component {
                        text("Hello")
                    }

                message.toMiniMessage() shouldBe "Hello"
                message.toMini() shouldBe "Hello"
            }

            "deserializes MiniMessage strings" {
                val message = "<red>Hello".mini()

                message shouldContainText "Hello"
                message shouldHaveColor NamedTextColor.RED
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

            "round-trips text hover payloads through MiniMessage" {
                val message =
                    component {
                        text("Hover") {
                            hover {
                                text("Tooltip")
                            }
                        }
                    }

                val roundTripped = MiniMessage.miniMessage().deserialize(message.toMiniMessage())

                roundTripped shouldHaveHoverText Component.text("Tooltip")
            }

            "round-trips item hover payload data components through MiniMessage" {
                val dataComponents =
                    mapOf<Key, DataComponentValue>(
                        key("minecraft", "custom_data") to BinaryTagHolder.binaryTagHolder("{kotventure:1b}"),
                    )
                val item = HoverEvent.ShowItem.showItem(key("minecraft", "diamond_sword"), 1, dataComponents)
                val message =
                    component {
                        text("Hover") {
                            hover {
                                item(
                                    key = key("minecraft", "diamond_sword"),
                                    dataComponents = dataComponents,
                                )
                            }
                        }
                    }

                val roundTripped = MiniMessage.miniMessage().deserialize(message.toMiniMessage())

                roundTripped shouldHaveHoverItem item
            }

            "round-trips entity hover payloads through MiniMessage" {
                val id = UUID.fromString("3f5f1f4e-29cb-4c98-93f0-3c7f4b52ddee")
                val entity =
                    HoverEvent.ShowEntity.showEntity(
                        key("minecraft", "player"),
                        id,
                        Component.text("Alex"),
                    )
                val message =
                    component {
                        text("Hover") {
                            hover {
                                entity(
                                    type = key("minecraft", "player"),
                                    id = id,
                                ) {
                                    text("Alex")
                                }
                            }
                        }
                    }

                val roundTripped = MiniMessage.miniMessage().deserialize(message.toMiniMessage())

                roundTripped shouldHaveHoverEntity entity
            }
        },
    )
