package io.github.lmliam.kotventure.core.event

import io.github.lmliam.kotventure.core.audience.emptyAudience
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.style.style
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldHaveClickAction
import io.github.lmliam.kotventure.test.text.shouldHaveClickEvent
import io.github.lmliam.kotventure.test.text.shouldHaveClickIntPayload
import io.github.lmliam.kotventure.test.text.shouldHaveClickTextPayload
import io.github.lmliam.kotventure.test.text.shouldNotHaveClickEvent
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
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
            "builds reusable text payload click event actions" {
                val events =
                    listOf(
                        click { openUrl("https://example.com") } to
                                (ClickEvent.Action.OPEN_URL to "https://example.com"),
                        click { openFile("/tmp/example.txt") } to
                                (ClickEvent.Action.OPEN_FILE to "/tmp/example.txt"),
                        click { run("/spawn") } to
                                (ClickEvent.Action.RUN_COMMAND to "/spawn"),
                        click { suggest("/msg Alex ") } to
                                (ClickEvent.Action.SUGGEST_COMMAND to "/msg Alex "),
                        click { copy("copied") } to
                                (ClickEvent.Action.COPY_TO_CLIPBOARD to "copied"),
                    )

                events.forEach { (event, expected) ->
                    val (action, payload) = expected
                    text("Action").clickEvent(event) shouldHaveClickAction action
                    text("Action").clickEvent(event) shouldHaveClickTextPayload payload
                }
            }

            "keeps explicit open url and open file click events" {
                val target =
                    Path
                        .of("build", "click-event-dsl-test.txt")
                        .toAbsolutePath()
                        .normalize()
                        .toUri()
                        .toString()
                val urlEvent =
                    click {
                        openUrl(target)
                    }
                val fileEvent =
                    click {
                        openFile("/tmp/example.txt")
                    }

                text("Url").clickEvent(urlEvent) shouldHaveClickAction ClickEvent.Action.OPEN_URL
                text("Url").clickEvent(urlEvent) shouldHaveClickTextPayload target
                text("File").clickEvent(fileEvent) shouldHaveClickAction ClickEvent.Action.OPEN_FILE
                text("File").clickEvent(fileEvent) shouldHaveClickTextPayload "/tmp/example.txt"
            }

            "builds reusable change page click events with integer payloads" {
                val event =
                    click {
                        changePage(4)
                    }

                text("Page").clickEvent(event) shouldHaveClickAction ClickEvent.Action.CHANGE_PAGE
                text("Page").clickEvent(event) shouldHaveClickIntPayload 4
            }

            "builds typed raw click events with Adventure validation" {
                val payload = ClickEvent.Payload.custom(key("kotventure", "claim"))
                val event = clickEvent(ClickEvent.Action.CUSTOM, payload)
                val component = component { click(ClickEvent.Action.CUSTOM, payload) }

                event.action() shouldBe ClickEvent.Action.CUSTOM
                event.payload() shouldBe payload
                component shouldHaveClickEvent event
            }

            "applies block click events through component scopes" {
                val component =
                    component {
                        text("Open") {
                            click {
                                openUrl("https://example.com")
                            }
                        }
                        text("Run") {
                            click {
                                run("/spawn")
                            }
                        }
                        text("Suggest") {
                            click {
                                suggest("/msg Alex ")
                            }
                        }
                        text("Copy") {
                            click {
                                copy("secret")
                            }
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
            }

            "applies reusable click events and block-built style click events" {
                val event =
                    click {
                        run("/spawn")
                    }
                val style =
                    style {
                        click {
                            copy("style-copy")
                        }
                    }
                val component =
                    component {
                        text("Reusable") {
                            click(event)
                        }
                        text("Styled") {
                            style(style)
                        }
                    }

                component.childAt(0) shouldHaveClickEvent event
                component.childAt(1) shouldHaveClickAction ClickEvent.Action.COPY_TO_CLIPBOARD
                component.childAt(1) shouldHaveClickTextPayload "style-copy"
            }

            "applies and clears raw click events through component and style scopes" {
                val manualEvent = ClickEvent.openUrl("https://example.org")
                val component =
                    component {
                        click(manualEvent)
                    }
                val styledComponent =
                    component {
                        style {
                            click(manualEvent)
                        }
                    }
                val clearedStyle =
                    component {
                        click(manualEvent)
                        style {
                            click(null)
                        }
                    }

                component shouldHaveClickEvent manualEvent
                styledComponent shouldHaveClickEvent manualEvent
                clearedStyle.shouldNotHaveClickEvent()
            }

            "rejects a second click event in one block" {
                val event =
                    click {
                        openUrl("https://example.org")
                    }

                shouldThrow<IllegalStateException> {
                    component {
                        click(event)
                        click(null)
                    }
                }
                shouldThrow<IllegalStateException> {
                    style {
                        click(event)
                        click(event)
                    }
                }
            }

            "propagates invalid change page validation errors" {
                shouldThrow<IllegalArgumentException> {
                    click {
                        changePage(0)
                    }
                }
            }

            "callback click events invoke the callback and pass options to Adventure" {
                RecordingClickCallbackProvider.reset()
                var calledWith: Audience? = null

                val event =
                    click {
                        callback(uses = 3, lifetime = 10.minutes) { audience ->
                            calledWith = audience
                        }
                    }
                val audience = emptyAudience()

                text("Callback").clickEvent(event) shouldHaveClickEvent
                        RecordingClickCallbackProvider.lastEvent
                RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 3
                RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe JavaDuration.ofMinutes(10)

                RecordingClickCallbackProvider.fire(audience)

                calledWith shouldBe audience
            }

            "bare callback click events invoke the callback" {
                RecordingClickCallbackProvider.reset()
                var calledWith: Audience? = null

                val event =
                    click {
                        callback { audience ->
                            calledWith = audience
                        }
                    }
                val audience = emptyAudience()

                text("Callback").clickEvent(event) shouldHaveClickEvent
                        RecordingClickCallbackProvider.lastEvent

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
                    click {
                        callback(options) {
                            // The provider capture is the assertion target for this test.
                        }
                    }

                text("Callback").clickEvent(event) shouldHaveClickEvent
                        RecordingClickCallbackProvider.lastEvent
                RecordingClickCallbackProvider.lastOptions shouldBe options
                RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 4
                RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe lifetime
            }

            "component scope callbacks accept kotlin durations inside click blocks" {
                RecordingClickCallbackProvider.reset()

                val component =
                    component {
                        click {
                            callback(uses = 1, lifetime = 5.minutes) {
                                // The provider capture is the assertion target for this test.
                            }
                        }
                    }

                component shouldHaveClickEvent RecordingClickCallbackProvider.lastEvent
                RecordingClickCallbackProvider.lastOptions?.uses() shouldBe 1
                RecordingClickCallbackProvider.lastOptions?.lifetime() shouldBe JavaDuration.ofMinutes(5)
            }

            "rejects empty click action blocks" {
                shouldThrow<IllegalStateException> {
                    click {
                    }
                }
            }

            "rejects click action blocks with multiple actions" {
                shouldThrow<IllegalStateException> {
                    click {
                        run("/one")
                        copy("two")
                    }
                }
            }

            "keeps direct action helpers out of component and style scopes" {
                assertDoesNotCompile(
                    "ComponentClickActionLeakTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.component.component

                    fun shouldNotCompile() {
                        component {
                            text("Open") {
                                openUrl("https://example.com")
                            }
                        }
                    }
                    """.trimIndent(),
                    "Unresolved reference 'openUrl'",
                )
                assertDoesNotCompile(
                    "StyleClickActionLeakTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.style.style

                    fun shouldNotCompile() {
                        style {
                            copy("secret")
                        }
                    }
                    """.trimIndent(),
                    "Unresolved reference 'copy'",
                )
            }

            "removes direct action helpers from explicit click scopes" {
                val helperCalls =
                    mapOf(
                        "OpenUrl" to
                                CompileFailureCase(
                                    """scope.openUrl("https://example.com")""",
                                    "Unresolved reference 'openUrl'",
                                ),
                        "OpenFile" to
                                CompileFailureCase(
                                    """scope.openFile("/tmp/example.txt")""",
                                    "Unresolved reference 'openFile'",
                                ),
                        "Run" to
                                CompileFailureCase(
                                    """scope.run("/spawn")""",
                                    "Argument type mismatch",
                                ),
                        "Suggest" to
                                CompileFailureCase(
                                    """scope.suggest("/msg Alex ")""",
                                    "Unresolved reference 'suggest'",
                                ),
                        "ChangePage" to
                                CompileFailureCase(
                                    """scope.changePage(2)""",
                                    "Unresolved reference 'changePage'",
                                ),
                        "Copy" to
                                CompileFailureCase(
                                    """scope.copy("secret")""",
                                    "Unresolved reference 'copy'",
                                ),
                        "Callback" to
                                CompileFailureCase(
                                    """
                                    val callback = ClickCallback<Audience> { }
                                    scope.callback(callback)
                                    """.trimIndent(),
                                    "Unresolved reference 'callback'",
                                ),
                        "CallbackWithOptions" to
                                CompileFailureCase(
                                    """
                                    val callback = ClickCallback<Audience> { }
                                    val options = ClickCallback.Options.builder().uses(1).build()
                                    scope.callback(options, callback)
                                    """.trimIndent(),
                                    "Unresolved reference 'callback'",
                                ),
                        "CallbackWithUsesAndLifetime" to
                                CompileFailureCase(
                                    """
                                    val callback = ClickCallback<Audience> { }
                                    scope.callback(1, 1.minutes, callback)
                                    """.trimIndent(),
                                    "Unresolved reference 'callback'",
                                ),
                    )

                helperCalls.forEach { (name, failure) ->
                    assertDoesNotCompile(
                        "Removed${name}ClickScopeHelperTest.kt",
                        """
                        import io.github.lmliam.kotventure.core.event.ClickScope
                        import net.kyori.adventure.audience.Audience
                        import net.kyori.adventure.text.event.ClickCallback
                        import kotlin.time.Duration.Companion.minutes

                        fun shouldNotCompile(scope: ClickScope) {
                            ${failure.source}
                        }
                        """.trimIndent(),
                        failure.expectedMessage,
                    )
                }
            }
        },
    )

private data class CompileFailureCase(
    val source: String,
    val expectedMessage: String,
)
