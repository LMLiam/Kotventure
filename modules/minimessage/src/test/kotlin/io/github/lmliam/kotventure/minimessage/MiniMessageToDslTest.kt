package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.text.component
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import java.util.UUID

class MiniMessageToDslTest :
    FunSpec({
        context("text and style emission") {
            test("generates snapshot-style source for the join-message example") {
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

            test("escapes Kotlin string content in generated source") {
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

            test("emits all standard text decorations") {
                val input = "<bold><italic><underlined><strikethrough><obfuscated>styled"

                miniToDsl(input) shouldBe
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

                val styled = mini(input)
                styled shouldHaveDecoration TextDecoration.BOLD
                styled shouldHaveDecoration TextDecoration.ITALIC
                styled shouldHaveDecoration TextDecoration.UNDERLINED
                styled shouldHaveDecoration TextDecoration.STRIKETHROUGH
                styled shouldHaveDecoration TextDecoration.OBFUSCATED
            }

            test("emits disabled decoration states that override inherited style") {
                val input = "<bold>hot <!bold>cold"

                miniToDsl(input) shouldBe
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

                val hot = mini(input)
                hot shouldHaveDecoration TextDecoration.BOLD
                hot.childAt(0).shouldHaveDecoration(TextDecoration.BOLD, TextDecoration.State.FALSE)
            }

            test("round-trips named colours and decorations against compiled expected DSL") {
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

            test("round-trips nested colours against compiled expected DSL") {
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

            test("renders an empty component for empty input") {
                val expected = component {}

                assertGoldenRoundTrip("", "component {}", expected)
            }
        }

        context("click event emission") {
            clickRoundTripCases.forEach { roundTrip ->
                test(roundTrip.name) {
                    assertGoldenRoundTrip(roundTrip.expectedSource, roundTrip.expectedComponent)
                }
            }

            test("emits a marker for non-representable click actions") {
                val payload = ClickEvent.Payload.custom(key("kotventure", "claim"))
                val expected =
                    component {
                        text("Claim") {
                            click(ClickEvent.Action.CUSTOM, payload)
                        }
                    }

                MiniMessageToDslWriter.write(expected) shouldBe
                        """
                    component {
                        text("Claim") {
                            click {
                                // callback not representable
                            }
                        }
                    }
                    """.trimIndent()
            }

            test("escapes Kotlin string content in click payloads") {
                val dollar = '$'
                val escapedDollar = "\\$dollar"
                val expected =
                    component {
                        text("Copy") {
                            click {
                                copy("say \\ \"hi\"\n\t${dollar}5\rcr")
                            }
                        }
                    }
                val expectedSource =
                    """
                    component {
                        text("Copy") {
                            click {
                                copy("say \\ \"hi\"\n\t${escapedDollar}5\rcr")
                            }
                        }
                    }
                    """.trimIndent()

                assertGoldenRoundTrip(expectedSource, expected)
            }
        }

        context("hover event emission") {
            hoverRoundTripCases.forEach { roundTrip ->
                test(roundTrip.name) {
                    assertGoldenRoundTrip(roundTrip.expectedSource, roundTrip.expectedComponent)
                }
            }
        }

        context("unsupported input") {
            test("rejects unsupported shadow colours instead of dropping them") {
                val error =
                    shouldThrow<IllegalArgumentException> {
                        miniToDsl("<shadow:red>shadow</shadow>")
                    }

                error.message shouldContain "miniToDsl slice 2 does not support shadow colours"
            }

            test("rejects unsupported component types") {
                val error =
                    shouldThrow<IllegalArgumentException> {
                        miniToDsl("<key:key.jump>")
                    }

                error.message shouldContain "supports only text component trees"
            }

            test("rejects unsupported component types in children") {
                val error =
                    shouldThrow<IllegalArgumentException> {
                        MiniMessageToDslWriter.write(Component.empty().append(Component.keybind("key.jump")))
                    }

                error.message shouldContain "supports only text component trees"
            }
        }

        context("literal MiniMessage input") {
            test("renders escaped MiniMessage tags as literal text") {
                val generated = miniToDsl("\\<red>literal")

                generated shouldBe
                        """
                    component {
                        text("<red>literal")
                    }
                    """.trimIndent()
                mini("\\<red>literal") shouldContainText "<red>literal"
            }
        }
    })

private val clickRoundTripCases: List<RoundTripCase> =
    listOf(
        RoundTripCase(
            name = "round-trips open url click events against compiled expected DSL",
            expectedSource =
                """
                component {
                    text("Open") {
                        click {
                            openUrl("https://example.com")
                        }
                    }
                }
                """.trimIndent(),
            expectedComponent =
                component {
                    text("Open") {
                        click {
                            openUrl("https://example.com")
                        }
                    }
                },
        ),
        RoundTripCase(
            name = "round-trips open file click events against compiled expected DSL",
            expectedSource =
                """
                component {
                    text("File") {
                        click {
                            openFile("/tmp/example.txt")
                        }
                    }
                }
                """.trimIndent(),
            expectedComponent =
                component {
                    text("File") {
                        click {
                            openFile("/tmp/example.txt")
                        }
                    }
                },
        ),
        RoundTripCase(
            name = "round-trips run command click events against compiled expected DSL",
            expectedSource =
                """
                component {
                    text("Spawn") {
                        click {
                            run("/spawn")
                        }
                    }
                }
                """.trimIndent(),
            expectedComponent =
                component {
                    text("Spawn") {
                        click {
                            run("/spawn")
                        }
                    }
                },
        ),
        RoundTripCase(
            name = "round-trips suggest command click events against compiled expected DSL",
            expectedSource =
                """
                component {
                    text("Reply") {
                        click {
                            suggest("/msg Alex ")
                        }
                    }
                }
                """.trimIndent(),
            expectedComponent =
                component {
                    text("Reply") {
                        click {
                            suggest("/msg Alex ")
                        }
                    }
                },
        ),
        RoundTripCase(
            name = "round-trips change page click events against compiled expected DSL",
            expectedSource =
                """
                component {
                    text("Page") {
                        click {
                            changePage(4)
                        }
                    }
                }
                """.trimIndent(),
            expectedComponent =
                component {
                    text("Page") {
                        click {
                            changePage(4)
                        }
                    }
                },
        ),
        RoundTripCase(
            name = "round-trips copy click events against compiled expected DSL",
            expectedSource =
                """
                component {
                    text("Copy") {
                        click {
                            copy("copied")
                        }
                    }
                }
                """.trimIndent(),
            expectedComponent =
                component {
                    text("Copy") {
                        click {
                            copy("copied")
                        }
                    }
                },
        ),
    )

private val hoverRoundTripCases: List<RoundTripCase> =
    listOf(
        RoundTripCase(
            name = "round-trips show text hover events against compiled expected DSL",
            expectedSource =
                """
                component {
                    text("Spawn") {
                        hover {
                            text {
                                text("Warp now") {
                                    color(NamedTextColor.GOLD)
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
            expectedComponent =
                component {
                    text("Spawn") {
                        hover {
                            text {
                                text("Warp now") {
                                    color(NamedTextColor.GOLD)
                                }
                            }
                        }
                    }
                },
        ),
        RoundTripCase(
            name = "round-trips show item hover events against compiled expected DSL",
            expectedSource =
                """
                component {
                    text("Loot") {
                        hover {
                            item(
                                key = key("minecraft", "diamond_sword"),
                                count = 2
                            )
                        }
                    }
                }
                """.trimIndent(),
            expectedComponent =
                component {
                    text("Loot") {
                        hover {
                            item(
                                key = key("minecraft", "diamond_sword"),
                                count = 2,
                            )
                        }
                    }
                },
        ),
        RoundTripCase(
            name = "round-trips show item hover data components against compiled expected DSL",
            expectedSource =
                """
                component {
                    text("Loot data") {
                        hover {
                            item(
                                key = key("minecraft", "diamond_sword"),
                                dataComponents = mapOf(
                                    key("minecraft", "custom_data") to BinaryTagHolder.binaryTagHolder("{kotventure:1b}")
                                )
                            )
                        }
                    }
                }
                """.trimIndent(),
            expectedComponent =
                component {
                    text("Loot data") {
                        hover {
                            item(
                                key = key("minecraft", "diamond_sword"),
                                dataComponents =
                                    mapOf<Key, DataComponentValue>(
                                        key("minecraft", "custom_data") to
                                                BinaryTagHolder.binaryTagHolder("{kotventure:1b}"),
                                    ),
                            )
                        }
                    }
                },
        ),
        RoundTripCase(
            name = "round-trips show entity hover events against compiled expected DSL",
            expectedSource =
                """
                component {
                    text("Mob") {
                        hover {
                            entity(
                                type = key("minecraft", "zombie"),
                                id = UUID.fromString("0d1630e2-fc7c-48ef-b7a0-8dfb9e57ec25")
                            )
                        }
                    }
                }
                """.trimIndent(),
            expectedComponent =
                component {
                    text("Mob") {
                        hover {
                            entity(
                                type = key("minecraft", "zombie"),
                                id = UUID.fromString("0d1630e2-fc7c-48ef-b7a0-8dfb9e57ec25"),
                            )
                        }
                    }
                },
        ),
        RoundTripCase(
            name = "round-trips named show entity hover events against compiled expected DSL",
            expectedSource =
                """
                component {
                    text("Named mob") {
                        hover {
                            entity(
                                type = key("minecraft", "player"),
                                id = UUID.fromString("3f5f1f4e-29cb-4c98-93f0-3c7f4b52ddee")
                            ) {
                                text("Alex \"\$5\"") {
                                    color(NamedTextColor.AQUA)
                                }
                            }
                        }
                    }
                }
                """.trimIndent(),
            expectedComponent =
                component {
                    text("Named mob") {
                        hover {
                            entity(
                                type = key("minecraft", "player"),
                                id = UUID.fromString("3f5f1f4e-29cb-4c98-93f0-3c7f4b52ddee"),
                            ) {
                                text("Alex \"$5\"") {
                                    color(NamedTextColor.AQUA)
                                }
                            }
                        }
                    }
                },
        ),
    )

private data class RoundTripCase(
    val name: String,
    val expectedSource: String,
    val expectedComponent: Component,
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

private fun assertGoldenRoundTrip(
    expectedSource: String,
    expectedComponent: Component,
) {
    val input = MiniMessage.miniMessage().serialize(expectedComponent)

    assertGoldenRoundTrip(input, expectedSource, expectedComponent)
}
