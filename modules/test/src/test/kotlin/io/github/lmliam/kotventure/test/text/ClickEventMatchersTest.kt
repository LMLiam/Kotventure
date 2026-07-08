package io.github.lmliam.kotventure.test.text

import io.github.lmliam.kotventure.core.text.text
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent

class ClickEventMatchersTest :
    StringSpec(
        {
            "matches root click events" {
                val clickEvent = ClickEvent.openUrl("https://example.com")

                text("Open").clickEvent(clickEvent) shouldHaveClickEvent clickEvent
            }

            "matches root click event actions" {
                Component
                    .text("Run")
                    .clickEvent(ClickEvent.runCommand("/spawn")) shouldHaveClickAction ClickEvent.Action.RUN_COMMAND
            }

            "matches text click payloads" {
                Component
                    .text("Copy")
                    .clickEvent(ClickEvent.copyToClipboard("secret")) shouldHaveClickTextPayload "secret"
            }

            "matches integer click payloads" {
                Component
                    .text("Page")
                    .clickEvent(ClickEvent.changePage(3)) shouldHaveClickIntPayload 3
            }

            "matches components without click events" {
                text("Plain").shouldNotHaveClickEvent()
            }

            "reports missing click events" {
                val expected = ClickEvent.openUrl("https://example.com")

                val failure =
                    shouldThrow<AssertionError> {
                        text("Plain") shouldHaveClickEvent expected
                    }
                val expectedMessage = "Expected click event <$expected>, but was <null>."

                failure.message shouldContain expectedMessage
            }

            "reports click action mismatches" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Run")
                            .clickEvent(ClickEvent.runCommand("/spawn")) shouldHaveClickAction
                                ClickEvent.Action.SUGGEST_COMMAND
                    }
                val expectedMessage =
                    "Expected click action <${ClickEvent.Action.SUGGEST_COMMAND}>, " +
                            "but was <${ClickEvent.Action.RUN_COMMAND}>."

                failure.message shouldContain expectedMessage
            }

            "reports text click payload mismatches" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Copy")
                            .clickEvent(ClickEvent.copyToClipboard("actual")) shouldHaveClickTextPayload "expected"
                    }
                val expectedMessage = "Expected click text payload <expected>, but was <actual>."

                failure.message shouldContain expectedMessage
            }

            "reports text click payload type mismatches" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Page")
                            .clickEvent(ClickEvent.changePage(2)) shouldHaveClickTextPayload "expected"
                    }
                val expectedMessage = "Expected click text payload <expected>, but was <integer payload <2>>."

                failure.message shouldContain expectedMessage
            }

            "reports integer click payload mismatches" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Page")
                            .clickEvent(ClickEvent.changePage(2)) shouldHaveClickIntPayload 3
                    }
                val expectedMessage = "Expected click integer payload <3>, but was <2>."

                failure.message shouldContain expectedMessage
            }

            "reports integer click payload type mismatches" {
                val failure =
                    shouldThrow<AssertionError> {
                        Component
                            .text("Copy")
                            .clickEvent(ClickEvent.copyToClipboard("two")) shouldHaveClickIntPayload 2
                    }
                val expectedMessage = "Expected click integer payload <2>, but was <text payload <two>>."

                failure.message shouldContain expectedMessage
            }

            "reports unexpected click events" {
                val actual = ClickEvent.suggestCommand("/help")

                val failure =
                    shouldThrow<AssertionError> {
                        text("Suggest").clickEvent(actual).shouldNotHaveClickEvent()
                    }
                val expectedMessage = "Expected click event to be absent, but was <$actual>."

                failure.message shouldContain expectedMessage
            }
        },
    )
