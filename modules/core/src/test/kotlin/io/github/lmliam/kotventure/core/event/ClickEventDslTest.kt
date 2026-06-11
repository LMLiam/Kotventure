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
import java.nio.file.Path
import kotlin.time.Duration.Companion.minutes
import java.time.Duration as JavaDuration

class ClickEventDslTest :
    StringSpec(
        {
            "builds every text payload click event action" {
                val components =
                    listOf(
                        component { openUrl("https://example.com") } to
                                (ClickEvent.Action.OPEN_URL to "https://example.com"),
                        component { openFile("/tmp/example.txt") } to
                                (ClickEvent.Action.OPEN_FILE to "/tmp/example.txt"),
                        component { runCommand("/spawn") } to
                                (ClickEvent.Action.RUN_COMMAND to "/spawn"),
                        component { suggestCommand("/msg Alex ") } to
                                (ClickEvent.Action.SUGGEST_COMMAND to "/msg Alex "),
                        component { copyToClipboard("copied") } to
                                (ClickEvent.Action.COPY_TO_CLIPBOARD to "copied"),
                    )

                components.forEach { (component, expected) ->
                    val (action, payload) = expected
                    component shouldHaveClickAction action
                    component shouldHaveClickTextPayload payload
                }
            }

            "builds change page click events with integer payloads" {
                val component = component { changePage(4) }

                component shouldHaveClickAction ClickEvent.Action.CHANGE_PAGE
                component shouldHaveClickIntPayload 4
            }

            "builds open and copy aliases" {
                val openComponent = component { open("https://example.com") }
                val copyComponent = component { copy("copied") }
                val openEvent = "https://example.org".asOpenClickEvent()
                val copyEvent = "also copied".asCopyClickEvent()

                openComponent shouldHaveClickAction ClickEvent.Action.OPEN_URL
                openComponent shouldHaveClickTextPayload "https://example.com"
                Component.text("Open").clickEvent(openEvent) shouldHaveClickTextPayload "https://example.org"
                copyComponent shouldHaveClickAction ClickEvent.Action.COPY_TO_CLIPBOARD
                copyComponent shouldHaveClickTextPayload "copied"
                Component.text("Copy").clickEvent(copyEvent) shouldHaveClickTextPayload "also copied"
            }

            "builds open file events from file uris" {
                val path =
                    Path
                        .of("build", "click-event-dsl-test.txt")
                        .toAbsolutePath()
                        .normalize()
                val component = component { open(path.toUri().toString()) }

                component shouldHaveClickAction ClickEvent.Action.OPEN_FILE
                component shouldHaveClickTextPayload path.toString()
            }

            "keeps explicit open url events for file uris" {
                val target =
                    Path
                        .of("build", "click-event-dsl-test.txt")
                        .toAbsolutePath()
                        .normalize()
                        .toUri()
                        .toString()
                val component = component { openUrl(target) }

                component shouldHaveClickAction ClickEvent.Action.OPEN_URL
                component shouldHaveClickTextPayload target
            }

            "falls back to open url events for file uris that are not paths" {
                val component = component { open("file:notes.txt") }

                component shouldHaveClickAction ClickEvent.Action.OPEN_URL
                component shouldHaveClickTextPayload "file:notes.txt"
            }

            "builds run and suggest aliases" {
                val runComponent = component { run("/spawn") }
                val suggestComponent = component { suggest("/msg Alex ") }

                runComponent shouldHaveClickAction ClickEvent.Action.RUN_COMMAND
                runComponent shouldHaveClickTextPayload "/spawn"
                suggestComponent shouldHaveClickAction ClickEvent.Action.SUGGEST_COMMAND
                suggestComponent shouldHaveClickTextPayload "/msg Alex "
            }

            "builds typed raw click events with Adventure validation" {
                val payload = ClickEvent.Payload.custom(key("kotventure", "claim"))
                val event = clickEvent(ClickEvent.Action.CUSTOM, payload)
                val component = component { click(ClickEvent.Action.CUSTOM, payload) }

                event.action() shouldBe ClickEvent.Action.CUSTOM
                event.payload() shouldBe payload
                component shouldHaveClickEvent event
            }

            "applies click events through component scopes" {
                val manualEvent = ClickEvent.openUrl("https://example.org")
                val component =
                    component {
                        text("Open") {
                            open("https://example.com")
                        }
                        text("Run") {
                            run("/spawn")
                        }
                        text("Suggest") {
                            suggest("/msg Alex ")
                        }
                        text("Copy") {
                            copy("secret")
                        }
                        text("Manual") {
                            click(manualEvent)
                        }
                    }

                component.childAt(0) shouldHaveClickAction ClickEvent.Action.OPEN_URL
                component.childAt(0) shouldHaveClickTextPayload "https://example.com"
                component.childAt(1) shouldHaveClickAction ClickEvent.Action.RUN_COMMAND
                component.childAt(1) shouldHaveClickTextPayload "/spawn"
                component.childAt(2) shouldHaveClickAction ClickEvent.Action.SUGGEST_COMMAND
                component.childAt(2) shouldHaveClickTextPayload "/msg Alex "
                component.childAt(3) shouldHaveClickAction ClickEvent.Action.COPY_TO_CLIPBOARD
                component.childAt(3) shouldHaveClickTextPayload "secret"
                component.childAt(4) shouldHaveClickAction ClickEvent.Action.OPEN_URL
                component.childAt(4) shouldHaveClickTextPayload "https://example.org"
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
                            click(null)
                        }
                    }

                component.shouldNotHaveClickEvent()
            }

            "propagates invalid change page validation errors" {
                val failure =
                    shouldThrow<IllegalArgumentException> {
                        component { changePage(0) }
                    }

                failure.message shouldContain "Change page payload integer must be greater than or equal to 1"
            }

            "callback click events invoke the callback and pass options to Adventure" {
                RecordingClickCallbackProvider.reset()
                var calledWith: Audience? = null
                val lifetime = JavaDuration.ofMinutes(10)

                val component =
                    component {
                        callback(uses = 3, lifetime = lifetime) { audience ->
                            calledWith = audience
                        }
                    }
                val event = RecordingClickCallbackProvider.lastEvent
                val audience = Audience.empty()

                component shouldHaveClickEvent event
                RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 3
                RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe lifetime

                RecordingClickCallbackProvider.fire(audience)

                calledWith shouldBe audience
            }

            "bare callback click events invoke the callback" {
                RecordingClickCallbackProvider.reset()
                var calledWith: Audience? = null

                val component =
                    component {
                        callback { audience ->
                            calledWith = audience
                        }
                    }
                val event = RecordingClickCallbackProvider.lastEvent
                val audience = Audience.empty()

                component shouldHaveClickEvent event

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

                val component =
                    component {
                        callback(options) {
                            // The provider capture is the assertion target for this test.
                        }
                    }
                val event = RecordingClickCallbackProvider.lastEvent

                component shouldHaveClickEvent event
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
