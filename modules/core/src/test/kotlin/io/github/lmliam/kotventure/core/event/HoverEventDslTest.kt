package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.style.style
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveHoverAction
import io.github.lmliam.kotventure.test.text.shouldHaveHoverEntity
import io.github.lmliam.kotventure.test.text.shouldHaveHoverEvent
import io.github.lmliam.kotventure.test.text.shouldHaveHoverItem
import io.github.lmliam.kotventure.test.text.shouldHaveHoverText
import io.github.lmliam.kotventure.test.text.shouldNotHaveHoverEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.key.Key
import net.kyori.adventure.key.Keyed
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import java.util.UUID

class HoverEventDslTest :
    StringSpec(
        {
            "builds reusable text hover events" {
                val event =
                    hover {
                        text("Tooltip") {
                            color(NamedTextColor.AQUA)
                        }
                    }

                event.action() shouldBe HoverEvent.Action.SHOW_TEXT
                val value = event.value() as Component
                value shouldContainText "Tooltip"
                value shouldHaveColor NamedTextColor.AQUA
            }

            "builds reusable item hover events with typed data components" {
                val dataComponents =
                    mapOf<Key, DataComponentValue>(
                        key("minecraft", "custom_data") to BinaryTagHolder.binaryTagHolder("{kotventure:1b}"),
                    )
                val event =
                    hover {
                        item(
                            key = key("minecraft", "diamond_sword"),
                            count = 2,
                            dataComponents = dataComponents,
                        )
                    }

                event.action() shouldBe HoverEvent.Action.SHOW_ITEM
                event.value() shouldBe
                        HoverEvent.ShowItem.showItem(key("minecraft", "diamond_sword"), 2, dataComponents)
            }

            "builds reusable item hover events from keyed values and permits zero counts" {
                val keyedItem = Keyed { key("minecraft", "stone") }
                val event =
                    hover {
                        item(
                            item = keyedItem,
                            count = 0,
                        )
                    }

                event.action() shouldBe HoverEvent.Action.SHOW_ITEM
                event.value() shouldBe HoverEvent.ShowItem.showItem(key("minecraft", "stone"), 0)
            }

            "builds reusable entity hover events with component names" {
                val id = UUID.fromString("3f5f1f4e-29cb-4c98-93f0-3c7f4b52ddee")
                val name = Component.text("Alex")
                val event =
                    hover {
                        entity(
                            type = key("minecraft", "player"),
                            id = id,
                            name = name,
                        )
                    }

                event.action() shouldBe HoverEvent.Action.SHOW_ENTITY
                event.value() shouldBe HoverEvent.ShowEntity.showEntity(key("minecraft", "player"), id, name)
            }

            "builds reusable entity hover events from keyed values" {
                val id = UUID.fromString("0d1630e2-fc7c-48ef-b7a0-8dfb9e57ec25")
                val entityType = Keyed { key("minecraft", "zombie") }
                val event =
                    hover {
                        entity(
                            type = entityType,
                            id = id,
                            name = Component.text("Zombie"),
                        )
                    }

                event.action() shouldBe HoverEvent.Action.SHOW_ENTITY
                event.value() shouldBe
                        HoverEvent.ShowEntity.showEntity(key("minecraft", "zombie"), id, Component.text("Zombie"))
            }

            "applies hover events through component scopes" {
                val id = UUID.fromString("0d1630e2-fc7c-48ef-b7a0-8dfb9e57ec25")
                val component =
                    component {
                        text("Text") {
                            hover {
                                text("Text hover")
                            }
                        }
                        text("Item") {
                            hover {
                                item(key("minecraft", "stone"))
                            }
                        }
                        text("Entity") {
                            hover {
                                entity(key("minecraft", "zombie"), id)
                            }
                        }
                    }

                component.childAt(0) shouldHaveHoverAction HoverEvent.Action.SHOW_TEXT
                component.childAt(0) shouldHaveHoverText Component.text("Text hover")
                component.childAt(1) shouldHaveHoverItem HoverEvent.ShowItem.showItem(key("minecraft", "stone"), 1)
                component.childAt(2) shouldHaveHoverEntity
                        HoverEvent.ShowEntity.showEntity(key("minecraft", "zombie"), id)
            }

            "builds rich hover text from nested component DSL content" {
                val component =
                    component {
                        text("Hover") {
                            hover {
                                text {
                                    text("First") {
                                        color(NamedTextColor.GREEN)
                                    }
                                    newline()
                                    text("Second") {
                                        color(NamedTextColor.DARK_GREEN)
                                    }
                                }
                            }
                        }
                    }

                val hoverText = component.childAt(0).hoverEvent()?.value() as Component

                hoverText shouldContainText "First"
                hoverText shouldContainText "Second"
                hoverText.childAt(0) shouldHaveColor NamedTextColor.GREEN
                hoverText.childAt(2) shouldHaveColor NamedTextColor.DARK_GREEN
            }

            "applies reusable hover events and styles" {
                val event =
                    hover {
                        text("Reusable")
                    }
                val style =
                    style {
                        hover(event)
                    }
                val component =
                    component {
                        text("Styled") {
                            style(style)
                        }
                        text("Inline") {
                            hover(event)
                        }
                    }

                component.childAt(0) shouldHaveHoverEvent event
                component.childAt(1) shouldHaveHoverEvent event
            }

            "clears hover events through component and style scopes" {
                val component =
                    component {
                        hover {
                            text("Tooltip")
                        }
                        hover(null)
                    }
                val styled =
                    component {
                        hover {
                            text("Tooltip")
                        }
                        style {
                            hover(null)
                        }
                    }

                component.shouldNotHaveHoverEvent()
                styled.shouldNotHaveHoverEvent()
            }

            "builds typed raw hover events with Adventure validation" {
                val value = Component.text("Raw")
                val event = hoverEvent(HoverEvent.Action.SHOW_TEXT, value)
                val component = component { hover(HoverEvent.Action.SHOW_TEXT, value) }

                event.action() shouldBe HoverEvent.Action.SHOW_TEXT
                event.value() shouldBe value
                component shouldHaveHoverEvent event
            }

            "rejects empty hover content blocks" {
                shouldThrow<IllegalStateException> {
                    hover {
                    }
                }
            }

            "rejects hover content blocks with multiple payloads" {
                shouldThrow<IllegalStateException> {
                    hover {
                        text("One")
                        item(key("minecraft", "stone"))
                    }
                }
            }

            "rejects negative item counts before calling Adventure" {
                shouldThrow<IllegalArgumentException> {
                    hover {
                        item(
                            key = key("minecraft", "stone"),
                            count = -1,
                        )
                    }
                }
            }
        },
    )
