package io.github.lmliam.kotventure.test.snapshot

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNot
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/** The component recorded in `styled-component.snapshot.json`; keep in sync with that committed fixture. */
private fun styledComponent(): Component =
    Component
        .text()
        .content("Hello ")
        .color(NamedTextColor.RED)
        .decorate(TextDecoration.BOLD)
        .append(Component.text("world", NamedTextColor.BLUE))
        .build()

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
                    Component.text("Hello") shouldMatchSnapshot "simple-text"
                }
            }

            "matches a styled component with children against its committed snapshot" {
                withSnapshotProperties {
                    styledComponent() shouldMatchSnapshot "styled-component"
                }
            }

            "returns the receiver so assertions chain" {
                withSnapshotProperties {
                    val component = Component.text("Hello")

                    (component shouldMatchSnapshot "simple-text") shouldBe component
                }
            }

            "composes as an ordinary matcher under shouldNot" {
                withSnapshotProperties {
                    Component.text("different") shouldNot matchSnapshot("simple-text")
                }
            }

            "does not record through a negated matcher assertion in record mode" {
                val target = tempDir.resolve("guard.snapshot.json")
                val original = "{\n  \"text\": \"keep\"\n}\n"
                target.writeText(original)

                withSnapshotProperties(update = true, dir = tempDir.toString()) {
                    Component.text("different") shouldNot matchSnapshot("guard")
                }

                target.readText() shouldBe original // the pure matcher must not write through shouldNot
            }

            "reports a mismatch with both expected and actual content" {
                withSnapshotProperties {
                    val failure =
                        shouldThrow<AssertionError> {
                            Component.text("Goodbye") shouldMatchSnapshot "simple-text"
                        }

                    failure.message shouldContain "does not match snapshot <simple-text>"
                    failure.message shouldContain "\"text\": \"Hello\"" // expected, from the committed fixture
                    failure.message shouldContain "\"text\": \"Goodbye\"" // actual, from the component under test
                }
            }

            "fails with record instructions when the snapshot is missing and record mode is off" {
                withSnapshotProperties(dir = tempDir.toString()) {
                    val failure =
                        shouldThrow<AssertionError> {
                            Component.text("Hello") shouldMatchSnapshot "absent"
                        }

                    failure.message shouldContain "No snapshot recorded for <absent>"
                    failure.message shouldContain "SNAPSHOT_UPDATE=true"
                }
            }

            "records a new snapshot in record mode and matches it afterwards" {
                withSnapshotProperties(update = true, dir = tempDir.toString()) {
                    Component.text("Hello") shouldMatchSnapshot "fresh"
                }

                val written = tempDir.resolve("fresh.snapshot.json")
                written.exists() shouldBe true
                written.readText() shouldContain "\"text\": \"Hello\""

                // The recorded snapshot now satisfies a plain comparison.
                withSnapshotProperties(dir = tempDir.toString()) {
                    Component.text("Hello") shouldMatchSnapshot "fresh"
                }
            }

            "overwrites an existing snapshot in record mode when the content differs" {
                val target = tempDir.resolve("stale.snapshot.json")
                target.writeText("{\n  \"text\": \"old\"\n}\n")

                withSnapshotProperties(update = true, dir = tempDir.toString()) {
                    Component.text("new") shouldMatchSnapshot "stale"
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
                    Component.text("Hello") shouldMatchSnapshot "simple-text"
                }

                target.readText() shouldBe before
            }
        },
    )
