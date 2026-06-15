package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.text.component
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage

class MiniMessageToDslTest :
    StringSpec(
        {
            "generates snapshot-style source for styled plain text" {
                miniToDsl("<red><bold>Hello") shouldBe
                        """
                    component {
                        text("Hello") {
                            color(NamedTextColor.RED)
                            bold()
                        }
                    }
                    """.trimIndent()
            }

            "generates snapshot-style source for nested children and hex colours" {
                miniToDsl("<gray>Hello <#12ab34>world</#12ab34></gray>") shouldBe
                        """
                    component {
                        text("Hello ") {
                            color(NamedTextColor.GRAY)
                            text("world") {
                                color(TextColor.color(0x12AB34))
                            }
                        }
                    }
                    """.trimIndent()
            }

            "generates snapshot-style source for the join-message example" {
                val input = "<gold>[<gray>Server</gray>]</gold> <aqua>Alex</aqua> joined the game"
                val expected =
                    component {
                        text("[") {
                            color(NamedTextColor.GOLD)
                            text("Server") {
                                color(NamedTextColor.GRAY)
                            }
                            text("]")
                        }
                        text(" ")
                        text("Alex") {
                            color(NamedTextColor.AQUA)
                        }
                        text(" joined the game")
                    }
                val expectedSource =
                    """
                    component {
                        text("[") {
                            color(NamedTextColor.GOLD)
                            text("Server") {
                                color(NamedTextColor.GRAY)
                            }
                            text("]")
                        }
                        text(" ")
                        text("Alex") {
                            color(NamedTextColor.AQUA)
                        }
                        text(" joined the game")
                    }
                    """.trimIndent()

                assertGoldenRoundTrip(input, expectedSource, expected)
            }

            "escapes Kotlin string content in generated source" {
                val dollar = '$'
                val escapedDollar = "\\$dollar"
                val input = "say \\ \"hi\"\n\t${dollar}5\rcr"

                miniToDsl(input) shouldBe
                        """
                    component {
                        text("say \\ \"hi\"\n\t${escapedDollar}5\rcr")
                    }
                    """.trimIndent()
            }

            "emits all standard text decorations" {
                miniToDsl("<bold><italic><underlined><strikethrough><obfuscated>styled") shouldBe
                        """
                    component {
                        text("styled") {
                            bold()
                            italic()
                            underlined()
                            strikethrough()
                            obfuscated()
                        }
                    }
                    """.trimIndent()
            }

            "emits disabled decoration states that override inherited style" {
                miniToDsl("<bold>hot <!bold>cold") shouldBe
                        """
                    component {
                        text("hot ") {
                            bold()
                            text("cold") {
                                style {
                                    bold(false)
                                }
                            }
                        }
                    }
                    """.trimIndent()
            }

            "rejects unsupported style metadata instead of dropping it" {
                val error =
                    shouldThrow<IllegalArgumentException> {
                        miniToDsl("<click:run_command:'/spawn'>spawn</click>")
                    }

                error.message shouldContain "miniToDsl slice 1 does not support click events"
            }

            "rejects unsupported component types" {
                val error =
                    shouldThrow<IllegalArgumentException> {
                        MiniMessageToDslWriter.write(Component.keybind("key.jump"))
                    }

                error.message shouldContain "supports only text component trees"
            }

            "renders escaped MiniMessage tags as literal text" {
                val generated = miniToDsl("\\<red>literal")

                generated shouldBe
                        """
                    component {
                        text("<red>literal")
                    }
                    """.trimIndent()
                mini("\\<red>literal") shouldContainText "<red>literal"
            }

            "round-trips named colours and decorations against compiled expected DSL" {
                val input = "<red><bold>Hello"
                val expected =
                    component {
                        text("Hello") {
                            color(NamedTextColor.RED)
                            bold()
                        }
                    }
                val expectedSource =
                    """
                    component {
                        text("Hello") {
                            color(NamedTextColor.RED)
                            bold()
                        }
                    }
                    """.trimIndent()

                assertGoldenRoundTrip(input, expectedSource, expected)
                expected shouldHaveChildCount 1
                expected.childAt(0) shouldHaveColor NamedTextColor.RED
                expected.childAt(0) shouldHaveDecoration TextDecoration.BOLD
            }

            "round-trips nested colours against compiled expected DSL" {
                val input = "<gray>Hello <#12ab34>world</#12ab34></gray>"
                val expected =
                    component {
                        text("Hello ") {
                            color(NamedTextColor.GRAY)
                            text("world") {
                                color(TextColor.color(0x12AB34))
                            }
                        }
                    }
                val expectedSource =
                    """
                    component {
                        text("Hello ") {
                            color(NamedTextColor.GRAY)
                            text("world") {
                                color(TextColor.color(0x12AB34))
                            }
                        }
                    }
                    """.trimIndent()

                assertGoldenRoundTrip(input, expectedSource, expected)
                expected shouldHaveChildCount 1
                expected.childAt(0) shouldHaveColor NamedTextColor.GRAY
                expected.childAt(0).childAt(0) shouldHaveColor TextColor.color(0x12AB34)
            }

            "renders an empty component for empty input" {
                val expected = component {}

                assertGoldenRoundTrip("", "component {}", expected)
            }
        },
    )

private fun assertGoldenRoundTrip(
    input: String,
    expectedSource: String,
    expectedComponent: Component,
) {
    val parsed = mini(input)

    miniToDsl(input) shouldBe expectedSource
    MiniMessage.miniMessage().serialize(parsed) shouldBe MiniMessage.miniMessage().serialize(expectedComponent)
}
