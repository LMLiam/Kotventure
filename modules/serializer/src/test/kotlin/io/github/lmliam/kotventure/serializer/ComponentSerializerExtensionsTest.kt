package io.github.lmliam.kotventure.serializer

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.color.green
import io.github.lmliam.kotventure.core.color.hex
import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.event.click
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.nbt.nbt
import io.github.lmliam.kotventure.core.objectcomponent.display
import io.github.lmliam.kotventure.core.objectcomponent.sprite
import io.github.lmliam.kotventure.core.text.text
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

class ComponentSerializerExtensionsTest :
    StringSpec(
        {
            "serializes component text to plain text" {
                val message =
                    component {
                        text("Hello ") {
                            color(aqua)
                        }
                        text("world") {
                            color(gold)
                        }
                    }

                message.toPlainText() shouldBe "Hello world"
            }

            "deserializes plain text strings" {
                val message = text("Hello world")

                message shouldContainText "Hello world"
                message.shouldNotHaveColor()
            }

            "round-trips legacy ampersand strings" {
                val message = "&aHello".asLegacyAmpersandComponent()

                message shouldContainText "Hello"
                message shouldHaveColor green
                message.toLegacyAmpersand() shouldBe "&aHello"
            }

            "round-trips legacy section strings" {
                val message = "\u00a7bHello".asLegacySectionComponent()

                message shouldContainText "Hello"
                message shouldHaveColor aqua
                message.toLegacySection() shouldBe "\u00a7bHello"
            }

            "round-trips JSON strings with color and events" {
                val spawnClick =
                    click {
                        run("/spawn")
                    }
                val message =
                    text("Portal") {
                        color(hex("#123ABC"))
                        click(spawnClick)
                        hover {
                            text("Teleport")
                        }
                    }

                val json = message.toJson()
                val roundTripped = json.asJsonComponent()

                roundTripped shouldContainText "Portal"
                roundTripped shouldHaveColor hex("#123ABC")
                roundTripped shouldHaveClickEvent spawnClick
                roundTripped shouldHaveHoverText text("Teleport")
            }

            "deserializes legacy item hover JSON strings" {
                val legacyJson =
                    """
                    {
                      "text": "Hover",
                      "hoverEvent": {
                        "action": "show_item",
                        "id": "minecraft:diamond_sword",
                        "Count": 1
                      }
                    }
                    """.trimIndent()
                val item =
                    HoverEvent.ShowItem.showItem(
                        key("minecraft", "diamond_sword"),
                        1,
                        null as BinaryTagHolder?,
                    )

                legacyJson.asJsonComponent() shouldHaveHoverItem item
            }

            "serializes unstyled component text to MiniMessage text" {
                val message =
                    component {
                        text("Hello")
                    }

                message.toMiniMessage() shouldBe "Hello"
            }

            "deserializes MiniMessage strings" {
                val message = MiniMessage.miniMessage().deserialize("<red>Hello")

                message shouldContainText "Hello"
                message shouldHaveColor red
            }

            "round-trips MiniMessage output through Adventure" {
                val message =
                    component {
                        text("Alert") {
                            color(red)
                        }
                    }

                val serialized = message.toMiniMessage()
                serialized shouldBe "<red>Alert"

                val roundTripped = MiniMessage.miniMessage().deserialize(serialized)

                roundTripped shouldContainText "Alert"
                roundTripped shouldHaveColor red
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
                            gradient(red, gold, aqua)
                        }
                    }

                val serialized = message.toMiniMessage()
                serialized shouldBe "<red>a</red><gold>c</gold><aqua>e"

                val roundTripped = MiniMessage.miniMessage().deserialize(serialized)

                roundTripped shouldContainText "ace"
                roundTripped shouldHaveChildCount 3
                roundTripped.childAt(0) shouldHaveColor red
                roundTripped.childAt(1) shouldHaveColor gold
                roundTripped.childAt(2) shouldHaveColor aqua
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

                roundTripped shouldHaveHoverText text("Tooltip")
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
                                item(key("minecraft", "diamond_sword")) {
                                    component(key("minecraft", "custom_data"), nbt("{kotventure:1b}"))
                                }
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
                        text("Alex"),
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
