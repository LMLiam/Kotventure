package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.text.text
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import java.util.UUID

class HoverEventMatchersTest :
    StringSpec(
        {
            "matches root hover events" {
                val hoverEvent = HoverEvent.showText(text("Tooltip"))

                text("Hover").hoverEvent(hoverEvent) shouldHaveHoverEvent hoverEvent
            }

            "matches root hover event actions" {
                text("Hover")
                    .hoverEvent(HoverEvent.showText(text("Tooltip"))) shouldHaveHoverAction
                        HoverEvent.Action.SHOW_TEXT
            }

            "matches text hover payloads" {
                text("Hover")
                    .hoverEvent(HoverEvent.showText(text("Tooltip"))) shouldHaveHoverText
                        text("Tooltip")
            }

            "matches item hover payloads" {
                val item = HoverEvent.ShowItem.showItem(key("minecraft", "diamond"), 2)

                text("Hover").hoverEvent(HoverEvent.showItem(item)) shouldHaveHoverItem item
            }

            "matches entity hover payloads" {
                val entity =
                    HoverEvent.ShowEntity.showEntity(
                        key("minecraft", "player"),
                        UUID.fromString("3f5f1f4e-29cb-4c98-93f0-3c7f4b52ddee"),
                        text("Alex"),
                    )

                text("Hover").hoverEvent(HoverEvent.showEntity(entity)) shouldHaveHoverEntity entity
            }

            "matches components without hover events" {
                text("Plain").shouldNotHaveHoverEvent()
            }

            "reports missing hover events" {
                val expected = HoverEvent.showText(text("Tooltip"))

                val failure =
                    shouldThrow<AssertionError> {
                        text("Plain") shouldHaveHoverEvent expected
                    }
                val expectedMessage = "Expected hover event <$expected>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "reports hover action mismatches" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("Hover")
                            .hoverEvent(HoverEvent.showText(text("Tooltip"))) shouldHaveHoverAction
                                HoverEvent.Action.SHOW_ITEM
                    }
                val expectedMessage =
                    "Expected hover action <${HoverEvent.Action.SHOW_ITEM}>, " +
                            "but was <${HoverEvent.Action.SHOW_TEXT}>."

                failure.message shouldContain expectedMessage
            }

            "reports text hover payload mismatches" {
                val failure =
                    shouldThrow<AssertionError> {
                        text("Hover")
                            .hoverEvent(HoverEvent.showText(text("actual"))) shouldHaveHoverText
                                text("expected")
                    }
                val expectedMessage =
                    "Expected hover text payload <${text("expected")}>, " +
                            "but was <${text("actual")}>."

                failure.message shouldContain expectedMessage
            }

            "reports text hover payload type mismatches" {
                val item = HoverEvent.ShowItem.showItem(key("minecraft", "stone"), 1)

                val failure =
                    shouldThrow<AssertionError> {
                        text("Hover").hoverEvent(HoverEvent.showItem(item)) shouldHaveHoverText
                                text("expected")
                    }
                val expectedMessage =
                    "Expected hover text payload <${text("expected")}>, " +
                            "but was <item payload <$item>>."

                failure.message shouldContain expectedMessage
            }

            "reports item hover payload mismatches" {
                val expected = HoverEvent.ShowItem.showItem(key("minecraft", "diamond"), 1)
                val actual = HoverEvent.ShowItem.showItem(key("minecraft", "stone"), 1)

                val failure =
                    shouldThrow<AssertionError> {
                        text("Hover").hoverEvent(HoverEvent.showItem(actual)) shouldHaveHoverItem expected
                    }
                val expectedMessage = "Expected hover item payload <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "reports item hover payload type mismatches" {
                val expected = HoverEvent.ShowItem.showItem(key("minecraft", "diamond"), 1)
                val text = text("Tooltip")

                val failure =
                    shouldThrow<AssertionError> {
                        text("Hover").hoverEvent(HoverEvent.showText(text)) shouldHaveHoverItem expected
                    }
                val expectedMessage = "Expected hover item payload <$expected>, but was <text payload <$text>>."

                failure.message shouldContain expectedMessage
            }

            "reports entity hover payload mismatches" {
                val expected =
                    HoverEvent.ShowEntity.showEntity(
                        key("minecraft", "player"),
                        UUID.fromString("3f5f1f4e-29cb-4c98-93f0-3c7f4b52ddee"),
                    )
                val actual =
                    HoverEvent.ShowEntity.showEntity(
                        key("minecraft", "player"),
                        UUID.fromString("0d1630e2-fc7c-48ef-b7a0-8dfb9e57ec25"),
                    )

                val failure =
                    shouldThrow<AssertionError> {
                        text("Hover").hoverEvent(HoverEvent.showEntity(actual)) shouldHaveHoverEntity expected
                    }
                val expectedMessage = "Expected hover entity payload <$expected>, but was <$actual>."

                failure.message shouldContain expectedMessage
            }

            "reports entity hover payload type mismatches" {
                val expected =
                    HoverEvent.ShowEntity.showEntity(
                        key("minecraft", "player"),
                        UUID.fromString("3f5f1f4e-29cb-4c98-93f0-3c7f4b52ddee"),
                    )
                val text = text("Tooltip")

                val failure =
                    shouldThrow<AssertionError> {
                        text("Hover").hoverEvent(HoverEvent.showText(text)) shouldHaveHoverEntity expected
                    }
                val expectedMessage = "Expected hover entity payload <$expected>, but was <text payload <$text>>."

                failure.message shouldContain expectedMessage
            }

            "reports unexpected hover events" {
                val actual = HoverEvent.showText(text("Tooltip"))

                val failure =
                    shouldThrow<AssertionError> {
                        text("Hover").hoverEvent(actual).shouldNotHaveHoverEvent()
                    }
                val expectedMessage = "Expected hover event to be absent, but was <$actual>."

                failure.message shouldContain expectedMessage
            }
        },
    )
