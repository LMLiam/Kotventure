package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.style.style
import io.github.lmliam.kotventure.core.text.component
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldHaveClickAction
import io.github.lmliam.kotventure.test.text.shouldHaveClickEvent
import io.github.lmliam.kotventure.test.text.shouldHaveClickIntPayload
import io.github.lmliam.kotventure.test.text.shouldHaveClickTextPayload
import io.github.lmliam.kotventure.test.text.shouldNotHaveClickEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickCallback
import net.kyori.adventure.text.event.ClickEvent
import kotlin.time.Duration.Companion.minutes
import java.time.Duration as JavaDuration

class ClickEventDslTest :
    StringSpec(
        {
            "builds every text payload click event action" {
                val clickEvents =
                    listOf(
                        openUrl("https://example.com") to
                                (ClickEvent.Action.OPEN_URL to "https://example.com"),
                        openFile("/tmp/example.txt") to
                                (ClickEvent.Action.OPEN_FILE to "/tmp/example.txt"),
                        runCommand("/spawn") to
                                (ClickEvent.Action.RUN_COMMAND to "/spawn"),
                        suggestCommand("/msg Alex ") to
                                (ClickEvent.Action.SUGGEST_COMMAND to "/msg Alex "),
                        copyToClipboard("copied") to
                                (ClickEvent.Action.COPY_TO_CLIPBOARD to "copied"),
                    )

                clickEvents.forEach { (event, expected) ->
                    val (action, payload) = expected
                    Component.text("Click").clickEvent(event) shouldHaveClickAction action
                    Component.text("Click").clickEvent(event) shouldHaveClickTextPayload payload
                }
            }

            "builds change page click events with integer payloads" {
                val event = changePage(4)

                Component.text("Next").clickEvent(event) shouldHaveClickAction ClickEvent.Action.CHANGE_PAGE
                Component.text("Next").clickEvent(event) shouldHaveClickIntPayload 4
            }

            "builds open and copy aliases" {
                val openEvent = open("https://example.com")
                val stringOpenEvent = "https://example.org".open()
                val copyEvent = copy("copied")
                val stringCopyEvent = "also copied".copy()

                Component.text("Open").clickEvent(openEvent) shouldHaveClickAction ClickEvent.Action.OPEN_URL
                Component.text("Open").clickEvent(openEvent) shouldHaveClickTextPayload "https://example.com"
                Component.text("Open").clickEvent(stringOpenEvent) shouldHaveClickTextPayload "https://example.org"
                Component.text("Copy").clickEvent(copyEvent) shouldHaveClickAction ClickEvent.Action.COPY_TO_CLIPBOARD
                Component.text("Copy").clickEvent(copyEvent) shouldHaveClickTextPayload "copied"
                Component.text("Copy").clickEvent(stringCopyEvent) shouldHaveClickTextPayload "also copied"
            }

            "builds typed raw click events with Adventure validation" {
                val payload = ClickEvent.Payload.custom(key("kotventure", "claim"))
                val event = clickEvent(ClickEvent.Action.CUSTOM, payload)

                event.action() shouldBe ClickEvent.Action.CUSTOM
                event.payload() shouldBe payload
            }

            "applies click events through component scopes" {
                val component =
                    component {
                        text("Open") {
                            open("https://example.com")
                        }
                        text("Copy") {
                            copy("secret")
                        }
                    }

                component.childAt(0) shouldHaveClickAction ClickEvent.Action.OPEN_URL
                component.childAt(0) shouldHaveClickTextPayload "https://example.com"
                component.childAt(1) shouldHaveClickAction ClickEvent.Action.COPY_TO_CLIPBOARD
                component.childAt(1) shouldHaveClickTextPayload "secret"
            }

            "applies reusable styles with click events" {
                val style =
                    style {
                        open("https://example.com")
                    }

                Component.text("Open").style(style) shouldHaveClickAction ClickEvent.Action.OPEN_URL
                Component.text("Open").style(style) shouldHaveClickTextPayload "https://example.com"
            }

            "clears click events through style scopes" {
                val component =
                    component {
                        openUrl("https://example.com")
                        style {
                            clickEvent(null)
                        }
                    }

                component.shouldNotHaveClickEvent()
            }

            "propagates invalid change page validation errors" {
                val failure =
                    shouldThrow<IllegalArgumentException> {
                        changePage(0)
                    }

                failure.message shouldContain "Change page payload integer must be greater than or equal to 1"
            }

            "callback click events invoke the callback and pass options to Adventure" {
                RecordingClickCallbackProvider.reset()
                var calledWith: Audience? = null
                val lifetime = JavaDuration.ofMinutes(10)

                val event =
                    callback(uses = 3, lifetime = lifetime) { audience ->
                        calledWith = audience
                    }
                val audience = Audience.empty()

                Component.text("Claim").clickEvent(event) shouldHaveClickEvent event
                RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 3
                RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe lifetime

                RecordingClickCallbackProvider.fire(audience)

                calledWith shouldBe audience
            }

            "bare callback click events invoke the callback" {
                RecordingClickCallbackProvider.reset()
                var calledWith: Audience? = null

                val event =
                    callback { audience ->
                        calledWith = audience
                    }
                val audience = Audience.empty()

                Component.text("Claim").clickEvent(event) shouldHaveClickEvent event

                RecordingClickCallbackProvider.fire(audience)

                calledWith shouldBe audience
            }

            "callback click events accept prebuilt options" {
                RecordingClickCallbackProvider.reset()
                val lifetime = JavaDuration.ofSeconds(45)
                val options =
                    ClickCallback.Options
                        .builder()
                        .uses(4)
                        .lifetime(lifetime)
                        .build()

                val event =
                    callback(options) {
                        // The provider capture is the assertion target for this test.
                    }

                Component.text("Claim").clickEvent(event) shouldHaveClickEvent event
                RecordingClickCallbackProvider.lastOptions shouldBe options
                RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 4
                RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe lifetime
            }

            "component scope callbacks pass options to Adventure" {
                RecordingClickCallbackProvider.reset()
                val lifetime = JavaDuration.ofSeconds(30)

                val component =
                    component {
                        callback(uses = 2, lifetime = lifetime) {
                            // The provider capture is the assertion target for this test.
                        }
                    }

                component shouldHaveClickEvent RecordingClickCallbackProvider.lastEvent
                RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 2
                RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe lifetime
            }

            "component scope callbacks accept kotlin durations" {
                RecordingClickCallbackProvider.reset()

                val component =
                    component {
                        callback(uses = 1, lifetime = 5.minutes) {
                            // The provider capture is the assertion target for this test.
                        }
                    }

                component shouldHaveClickEvent RecordingClickCallbackProvider.lastEvent
                RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 1
                RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe JavaDuration.ofMinutes(5)
            }
        },
    )
