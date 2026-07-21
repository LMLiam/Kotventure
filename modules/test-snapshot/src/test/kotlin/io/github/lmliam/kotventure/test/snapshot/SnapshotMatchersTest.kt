package io.github.lmliam.kotventure.test.snapshot

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.blue
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.color.gray
import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.color.yellow
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.keybind.keybind
import io.github.lmliam.kotventure.core.score.score
import io.github.lmliam.kotventure.core.selector.entities
import io.github.lmliam.kotventure.core.selector.selector
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.translatable.translatable
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/** The component in `styled-component.snapshot.json`. Keep it consistent with that committed fixture. */
private fun styledComponent(): Component =
    text("Hello ") {
        color(red)
        bold()
        text("world") {
            color(blue)
        }
    }

/**
 * Provides the component in `rich-message.snapshot.json`.
 *
 * The fixture covers styles, events, translatable content, keybinds, scores, and selectors. Keep it
 * consistent with the committed snapshot.
 */
private fun richMessage(): Component =
    text("Welcome, ") {
        color(gold)
        bold()
        font(key("minecraft:uniform"))
        click {
            openUrl("https://example.com/docs")
        }
        hover {
            text("Open the documentation")
        }
        translatable("multiplayer.player.joined") {
            fallback("%s joined the game")
            arg {
                content("Steve")
                color(aqua)
            }
        }
        text(" — jump with ")
        keybind("key.jump") {
            color(yellow)
        }
        text(" — deaths ")
        score("@p", "deaths")
        text(" — nearby ")
        selector(
            entities {
                type("minecraft:armor_stand")
                limit(3)
            },
        )
        text("!") {
            color(gray)
            italic(false)
        }
    }

class SnapshotMatchersTest :
    StringSpec(
        {
            lateinit var tempDir: Path

            beforeTest {
                tempDir = Files.createTempDirectory("kotventure-snapshot")
            }

            afterTest {
                tempDir.toFile().deleteRecursively()
            }

            "matches a plain text component against its committed snapshot" {
                withSnapshotProperties {
                    text("Hello") shouldMatchSnapshot "simple-text"
                }
            }

            "matches a styled component with children against its committed snapshot" {
                withSnapshotProperties {
                    styledComponent() shouldMatchSnapshot "styled-component"
                }
            }

            "matches a rich multi-type message against its committed snapshot" {
                withSnapshotProperties {
                    richMessage() shouldMatchSnapshot "rich-message"
                }
            }

            "returns the receiver so assertions chain" {
                withSnapshotProperties {
                    val component = text("Hello")

                    (component shouldMatchSnapshot "simple-text") shouldBe component
                }
            }

            "composes as an ordinary matcher under shouldNot" {
                withSnapshotProperties {
                    text("different") shouldNot matchSnapshot("simple-text")
                }
            }

            "does not record through a negated matcher assertion in record mode" {
                val target = tempDir.resolve("guard.snapshot.json")
                val original = "{\n  \"text\": \"keep\"\n}\n"
                target.writeText(original)

                withSnapshotProperties(update = true, dir = tempDir.toString()) {
                    text("different") shouldNot matchSnapshot("guard")
                }

                target.readText() shouldBe original
            }

            "reports a mismatch with both expected and actual content" {
                withSnapshotProperties {
                    val failure =
                        shouldThrow<AssertionError> {
                            text("Goodbye") shouldMatchSnapshot "simple-text"
                        }

                    failure.message shouldContain "does not match snapshot <simple-text>"
                    failure.message shouldContain "\"text\": \"Hello\""
                    failure.message shouldContain "\"text\": \"Goodbye\""
                }
            }

            "fails with record instructions when the snapshot is missing and record mode is off" {
                withSnapshotProperties(dir = tempDir.toString()) {
                    val failure =
                        shouldThrow<AssertionError> {
                            text("Hello") shouldMatchSnapshot "absent"
                        }

                    failure.message shouldContain "No snapshot recorded for <absent>"
                    failure.message shouldContain "SNAPSHOT_UPDATE=true"
                }
            }

            "records a new snapshot in record mode and matches it afterwards" {
                withSnapshotProperties(update = true, dir = tempDir.toString()) {
                    text("Hello") shouldMatchSnapshot "fresh"
                }

                val written = tempDir.resolve("fresh.snapshot.json")
                written.exists() shouldBe true
                written.readText() shouldContain "\"text\": \"Hello\""

                withSnapshotProperties(dir = tempDir.toString()) {
                    text("Hello") shouldMatchSnapshot "fresh"
                }
            }

            "overwrites an existing snapshot in record mode when the content differs" {
                val target = tempDir.resolve("stale.snapshot.json")
                target.writeText("{\n  \"text\": \"old\"\n}\n")

                withSnapshotProperties(update = true, dir = tempDir.toString()) {
                    text("new") shouldMatchSnapshot "stale"
                }

                val updated = target.readText()
                updated shouldContain "\"text\": \"new\""
                updated shouldNotContain "\"old\""
            }

            "does not write a snapshot when record mode is off and the component matches" {
                val target = tempDir.resolve("simple-text.snapshot.json")
                target.writeText("{\n  \"text\": \"Hello\"\n}\n")
                val before = target.readText()

                withSnapshotProperties(dir = tempDir.toString()) {
                    text("Hello") shouldMatchSnapshot "simple-text"
                }

                target.readText() shouldBe before
            }
        },
    )
