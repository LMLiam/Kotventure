package io.github.lmliam.kotventure.core.event

import com.tschuchort.compiletesting.KotlinCompilation
import com.tschuchort.compiletesting.SourceFile
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
import org.jetbrains.kotlin.compiler.plugin.ExperimentalCompilerApi
import java.nio.file.Path
import kotlin.time.Duration.Companion.minutes
import java.time.Duration as JavaDuration

@OptIn(ExperimentalCompilerApi::class)
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
                    Component.text("Action").clickEvent(event) shouldHaveClickAction action
                    Component.text("Action").clickEvent(event) shouldHaveClickTextPayload payload
                }
            }

            "builds reusable auto-target open click events" {
                val path =
                    Path
                        .of("build", "click-event-dsl-test.txt")
                        .toAbsolutePath()
                        .normalize()
                val urlEvent =
                    click {
                        open("https://example.com")
                    }
                val fileEvent =
                    click {
                        open(path.toUri().toString())
                    }

                Component.text("Url").clickEvent(urlEvent) shouldHaveClickAction ClickEvent.Action.OPEN_URL
                Component.text("Url").clickEvent(urlEvent) shouldHaveClickTextPayload "https://example.com"
                Component.text("File").clickEvent(fileEvent) shouldHaveClickAction ClickEvent.Action.OPEN_FILE
                Component.text("File").clickEvent(fileEvent) shouldHaveClickTextPayload path.toString()
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

                Component.text("Url").clickEvent(urlEvent) shouldHaveClickAction ClickEvent.Action.OPEN_URL
                Component.text("Url").clickEvent(urlEvent) shouldHaveClickTextPayload target
                Component.text("File").clickEvent(fileEvent) shouldHaveClickAction ClickEvent.Action.OPEN_FILE
                Component.text("File").clickEvent(fileEvent) shouldHaveClickTextPayload "/tmp/example.txt"
            }

            "builds reusable change page click events with integer payloads" {
                val event =
                    click {
                        changePage(4)
                    }

                Component.text("Page").clickEvent(event) shouldHaveClickAction ClickEvent.Action.CHANGE_PAGE
                Component.text("Page").clickEvent(event) shouldHaveClickIntPayload 4
            }

            "falls back to open url events for non-path targets" {
                val events =
                    listOf(
                        click { open("file:notes.txt") } to "file:notes.txt",
                        click { open("example.com") } to "example.com",
                        click { open("/server/rules") } to "/server/rules",
                        click { open("relative/path") } to "relative/path",
                    )

                events.forEach { (event, target) ->
                    Component.text("Open").clickEvent(event) shouldHaveClickAction ClickEvent.Action.OPEN_URL
                    Component.text("Open").clickEvent(event) shouldHaveClickTextPayload target
                }
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
                                open("https://example.com")
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
                val clearedComponent =
                    component {
                        click(manualEvent)
                        click(null)
                    }
                val clearedStyle =
                    component {
                        click(manualEvent)
                        style {
                            click(null)
                        }
                    }

                component shouldHaveClickEvent manualEvent
                clearedComponent.shouldNotHaveClickEvent()
                clearedStyle.shouldNotHaveClickEvent()
            }

            "propagates invalid change page validation errors" {
                val failure =
                    shouldThrow<IllegalArgumentException> {
                        click {
                            changePage(0)
                        }
                    }

                failure.message shouldContain "Change page payload integer must be greater than or equal to 1"
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
                val audience = Audience.empty()

                Component.text("Callback").clickEvent(event) shouldHaveClickEvent
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
                val audience = Audience.empty()

                Component.text("Callback").clickEvent(event) shouldHaveClickEvent
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

                Component.text("Callback").clickEvent(event) shouldHaveClickEvent
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
                val failure =
                    shouldThrow<IllegalStateException> {
                        click {
                        }
                    }

                failure.message shouldContain "choose exactly one action"
            }

            "rejects click action blocks with multiple actions" {
                val failure =
                    shouldThrow<IllegalStateException> {
                        click {
                            run("/one")
                            copy("two")
                        }
                    }

                failure.message shouldContain "choose only one"
            }

            "keeps direct action helpers out of component and style scopes" {
                assertDoesNotCompile(
                    "ComponentClickActionLeakTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.text.component

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
                        "Open" to """scope.open("https://example.com")""",
                        "OpenUrl" to """scope.openUrl("https://example.com")""",
                        "OpenFile" to """scope.openFile("/tmp/example.txt")""",
                        "Run" to """scope.run("/spawn")""",
                        "Suggest" to """scope.suggest("/msg Alex ")""",
                        "ChangePage" to """scope.changePage(2)""",
                        "Copy" to """scope.copy("secret")""",
                        "Callback" to
                                """
                                val callback = ClickCallback<Audience> { }
                                scope.callback(callback)
                                """.trimIndent(),
                        "CallbackWithOptions" to
                                """
                                val callback = ClickCallback<Audience> { }
                                val options = ClickCallback.Options.builder().uses(1).build()
                                scope.callback(options, callback)
                                """.trimIndent(),
                        "CallbackWithUsesAndLifetime" to
                                """
                                val callback = ClickCallback<Audience> { }
                                scope.callback(1, 1.minutes, callback)
                                """.trimIndent(),
                    )

                helperCalls.forEach { (name, call) ->
                    assertDoesNotCompile(
                        "Removed${name}ClickScopeHelperTest.kt",
                        """
                        import io.github.lmliam.kotventure.core.event.ClickScope
                        import net.kyori.adventure.audience.Audience
                        import net.kyori.adventure.text.event.ClickCallback
                        import kotlin.time.Duration.Companion.minutes

                        fun shouldNotCompile(scope: ClickScope) {
                            $call
                        }
                        """.trimIndent(),
                    )
                }
            }
        },
    )

@OptIn(ExperimentalCompilerApi::class)
private fun assertDoesNotCompile(
    fileName: String,
    source: String,
    expectedMessage: String? = null,
) {
    val compilation =
        KotlinCompilation().apply {
            inheritClassPath = true
            sources = listOf(SourceFile.kotlin(fileName, source))
        }

    val result = compilation.compile()

    result.exitCode shouldBe KotlinCompilation.ExitCode.COMPILATION_ERROR
    if (expectedMessage != null) {
        result.messages shouldContain expectedMessage
    }
}
