package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.nbt.blockPos
import io.github.lmliam.kotventure.core.objectcomponent.sprite
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
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.`object`.ObjectContents
import java.util.UUID

class MiniMessageToDslTest :
    FunSpec(
        {
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

                test("emits font styles inside a style block") {
                    val styled =
                        component {
                            text("title") {
                                style { font(key("minecraft", "uniform")) }
                            }
                        }

                    MiniMessageToDslWriter.write(styled) shouldBe
                            """
                    component {
                        text("title") {
                            style {
                                font(key("minecraft", "uniform"))
                            }
                        }
                    }
                    """.trimIndent()
                }

                test("emits insertion text inside a style block") {
                    val styled =
                        component {
                            text("Alex") {
                                style { insertion("/msg Alex ") }
                            }
                        }

                    MiniMessageToDslWriter.write(styled) shouldBe
                            """
                    component {
                        text("Alex") {
                            style {
                                insertion("/msg Alex ")
                            }
                        }
                    }
                    """.trimIndent()
                }

                test("groups font, insertion, and disabled decorations into one style block") {
                    val styled =
                        component {
                            text("badge") {
                                bold()
                                style {
                                    font(key("minecraft", "uniform"))
                                    insertion("/claim")
                                    italic(false)
                                }
                            }
                        }

                    MiniMessageToDslWriter.write(styled) shouldBe
                            """
                    component {
                        text("badge") {
                            bold()
                            style {
                                font(key("minecraft", "uniform"))
                                insertion("/claim")
                                italic(false)
                            }
                        }
                    }
                    """.trimIndent()
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
                test("round-trips open url click events against compiled expected DSL") {
                    assertGoldenRoundTrip(
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
                                    click { openUrl("https://example.com") }
                                }
                            },
                    )
                }

                test("round-trips open file click events against compiled expected DSL") {
                    assertGoldenRoundTrip(
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
                                    click { openFile("/tmp/example.txt") }
                                }
                            },
                    )
                }

                test("round-trips run command click events against compiled expected DSL") {
                    assertGoldenRoundTrip(
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
                                    click { run("/spawn") }
                                }
                            },
                    )
                }

                test("round-trips suggest command click events against compiled expected DSL") {
                    assertGoldenRoundTrip(
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
                                    click { suggest("/msg Alex ") }
                                }
                            },
                    )
                }

                test("round-trips change page click events against compiled expected DSL") {
                    assertGoldenRoundTrip(
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
                                    click { changePage(4) }
                                }
                            },
                    )
                }

                test("round-trips copy click events against compiled expected DSL") {
                    assertGoldenRoundTrip(
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
                                    click { copy("copied") }
                                }
                            },
                    )
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
                test("round-trips show text hover events against compiled expected DSL") {
                    assertGoldenRoundTrip(
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
                    )
                }

                test("round-trips show item hover events against compiled expected DSL") {
                    assertGoldenRoundTrip(
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
                    )
                }

                test("round-trips show item hover data components against compiled expected DSL") {
                    assertGoldenRoundTrip(
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
                    )
                }

                test("emits show item data components in a stable key order") {
                    val loot =
                        component {
                            text("Loot data") {
                                hover {
                                    item(
                                        key = key("minecraft", "diamond_sword"),
                                        dataComponents =
                                            mapOf<Key, DataComponentValue>(
                                                key(
                                                    "minecraft",
                                                    "damage",
                                                ) to BinaryTagHolder.binaryTagHolder("{value:5b}"),
                                                key("minecraft", "custom_data") to
                                                        BinaryTagHolder.binaryTagHolder("{kotventure:1b}"),
                                            ),
                                    )
                                }
                            }
                        }

                    MiniMessageToDslWriter.write(loot) shouldBe
                            """
                    component {
                        text("Loot data") {
                            hover {
                                item(
                                    key = key("minecraft", "diamond_sword"),
                                    dataComponents = mapOf(
                                        key("minecraft", "custom_data") to BinaryTagHolder.binaryTagHolder("{kotventure:1b}"),
                                        key("minecraft", "damage") to BinaryTagHolder.binaryTagHolder("{value:5b}")
                                    )
                                )
                            }
                        }
                    }
                    """.trimIndent()
                }

                test("round-trips show entity hover events against compiled expected DSL") {
                    assertGoldenRoundTrip(
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
                    )
                }

                test("round-trips named show entity hover events against compiled expected DSL") {
                    assertGoldenRoundTrip(
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
                    )
                }
            }

            context("structured component emission") {
                test("round-trips keybind components against compiled expected DSL") {
                    assertGoldenRoundTrip(
                        input = "<key:key.jump>",
                        expectedSource =
                            """
                        component {
                            keybind("key.jump")
                        }
                        """.trimIndent(),
                        expectedComponent = component { keybind("key.jump") },
                    )
                }

                test("round-trips keybind components carrying style against compiled expected DSL") {
                    assertGoldenRoundTrip(
                        expectedSource =
                            """
                        component {
                            keybind("key.sneak") {
                                color(NamedTextColor.GREEN)
                            }
                        }
                        """.trimIndent(),
                        expectedComponent =
                            component {
                                keybind("key.sneak") {
                                    color(NamedTextColor.GREEN)
                                }
                            },
                    )
                }

                test("round-trips keybind components carrying click events against compiled expected DSL") {
                    assertGoldenRoundTrip(
                        expectedSource =
                            """
                        component {
                            keybind("key.jump") {
                                click {
                                    run("/help")
                                }
                            }
                        }
                        """.trimIndent(),
                        expectedComponent =
                            component {
                                keybind("key.jump") {
                                    click { run("/help") }
                                }
                            },
                    )
                }

                test("round-trips score components against compiled expected DSL") {
                    assertGoldenRoundTrip(
                        input = "<score:player:objective>",
                        expectedSource =
                            """
                        component {
                            score("player", "objective")
                        }
                        """.trimIndent(),
                        expectedComponent = component { score("player", "objective") },
                    )
                }

                test("round-trips selector components against compiled expected DSL") {
                    assertGoldenRoundTrip(
                        input = "<selector:'@p'>",
                        expectedSource =
                            """
                        component {
                            selector("@p")
                        }
                        """.trimIndent(),
                        expectedComponent = component { selector("@p") },
                    )
                }

                test("round-trips selector separators against compiled expected DSL") {
                    assertGoldenRoundTrip(
                        input = "<selector:'@e':', '>",
                        expectedSource =
                            """
                        component {
                            selector("@e") {
                                separator {
                                    text(", ")
                                }
                            }
                        }
                        """.trimIndent(),
                        expectedComponent =
                            component {
                                selector("@e") {
                                    separator { text(", ") }
                                }
                            },
                    )
                }

                test("round-trips argument-free translatable components against compiled expected DSL") {
                    assertGoldenRoundTrip(
                        input = "<lang:death.fell.accident.ladder>",
                        expectedSource =
                            """
                        component {
                            translatable("death.fell.accident.ladder")
                        }
                        """.trimIndent(),
                        expectedComponent = component { translatable("death.fell.accident.ladder") },
                    )
                }

                test("round-trips translatable arguments against compiled expected DSL") {
                    assertGoldenRoundTrip(
                        input = "<lang:multiplayer.player.joined:Alex>",
                        expectedSource =
                            """
                        component {
                            translatable("multiplayer.player.joined") {
                                arg {
                                    text("Alex")
                                }
                            }
                        }
                        """.trimIndent(),
                        expectedComponent =
                            component {
                                translatable("multiplayer.player.joined") {
                                    arg { text("Alex") }
                                }
                            },
                    )
                }

                test("recurses nested styled translatable arguments against compiled expected DSL") {
                    assertGoldenRoundTrip(
                        input = "<lang:'k':'<red>x<bold>y'>",
                        expectedSource =
                            """
                        component {
                            translatable("k") {
                                arg {
                                    text("x") {
                                        color(NamedTextColor.RED)
                                        text("y") {
                                            bold()
                                        }
                                    }
                                }
                            }
                        }
                        """.trimIndent(),
                        expectedComponent =
                            component {
                                translatable("k") {
                                    arg {
                                        text("x") {
                                            color(NamedTextColor.RED)
                                            text("y") { bold() }
                                        }
                                    }
                                }
                            },
                    )
                }

                test("round-trips translatable components nesting other structured components") {
                    assertGoldenRoundTrip(
                        input = "<lang:k:a>:<lang:k:b>",
                        expectedSource =
                            """
                        component {
                            translatable("k") {
                                arg {
                                    text("a")
                                }
                                text(":")
                                translatable("k") {
                                    arg {
                                        text("b")
                                    }
                                }
                            }
                        }
                        """.trimIndent(),
                        expectedComponent =
                            component {
                                translatable("k") {
                                    arg { text("a") }
                                    text(":")
                                    translatable("k") {
                                        arg { text("b") }
                                    }
                                }
                            },
                    )
                }

                test("emits translatable fallback text") {
                    val translatable =
                        component {
                            translatable("menu.singleplayer") {
                                fallback("Singleplayer")
                            }
                        }

                    MiniMessageToDslWriter.write(translatable) shouldBe
                            """
                    component {
                        translatable("menu.singleplayer") {
                            fallback("Singleplayer")
                        }
                    }
                    """.trimIndent()
                }

                test("emits a structured component carrying arguments, style, and children together") {
                    val translatable =
                        component {
                            translatable("commands.give.success.single") {
                                arg { text("Alex") }
                                color(NamedTextColor.GREEN)
                                text("!")
                            }
                        }

                    MiniMessageToDslWriter.write(translatable) shouldBe
                            """
                    component {
                        translatable("commands.give.success.single") {
                            arg {
                                text("Alex")
                            }
                            color(NamedTextColor.GREEN)
                            text("!")
                        }
                    }
                    """.trimIndent()
                }
            }

            context("NBT component emission") {
                test("emits bare block NBT components from a compiled expected DSL") {
                    val nbt = component { blockNbt(blockPos("1 64 -3"), "Items") }

                    MiniMessageToDslWriter.write(nbt) shouldBe
                            """
                    component {
                        blockNbt(blockPos("1 64 -3"), "Items")
                    }
                    """.trimIndent()
                }

                test("emits block NBT interpretation, separators, and style together") {
                    val nbt =
                        component {
                            blockNbt(blockPos("1 64 -3"), "Items") {
                                interpret(true)
                                separator { text(", ") }
                                color(NamedTextColor.AQUA)
                            }
                        }

                    MiniMessageToDslWriter.write(nbt) shouldBe
                            """
                    component {
                        blockNbt(blockPos("1 64 -3"), "Items") {
                            interpret(true)
                            separator {
                                text(", ")
                            }
                            color(NamedTextColor.AQUA)
                        }
                    }
                    """.trimIndent()
                }

                test("emits entity NBT components from a compiled expected DSL") {
                    val nbt = component { entityNbt("@e[type=armor_stand]", "Pos") }

                    MiniMessageToDslWriter.write(nbt) shouldBe
                            """
                    component {
                        entityNbt("@e[type=armor_stand]", "Pos")
                    }
                    """.trimIndent()
                }

                test("emits storage NBT components carrying interpretation") {
                    val nbt =
                        component {
                            storageNbt(key("minecraft", "data"), "Contents") {
                                interpret(true)
                            }
                        }

                    MiniMessageToDslWriter.write(nbt) shouldBe
                            """
                    component {
                        storageNbt(key("minecraft", "data"), "Contents") {
                            interpret(true)
                        }
                    }
                    """.trimIndent()
                }
            }

            context("object component emission") {
                test("emits sprite contents that use the default atlas with the single-argument form") {
                    val display = component { display(sprite(key("minecraft", "icon/star"))) }

                    MiniMessageToDslWriter.write(display) shouldBe
                            """
                    component {
                        display(sprite(key("minecraft", "icon/star")))
                    }
                    """.trimIndent()
                }

                test("emits sprite contents from a non-default atlas with the two-argument form") {
                    val display =
                        component {
                            display(sprite(key("minecraft", "gui"), key("minecraft", "icon/heart")))
                        }

                    MiniMessageToDslWriter.write(display) shouldBe
                            """
                    component {
                        display(sprite(key("minecraft", "gui"), key("minecraft", "icon/heart")))
                    }
                    """.trimIndent()
                }

                test("emits object fallbacks and style together") {
                    val display =
                        component {
                            display(sprite(key("minecraft", "icon/heart"))) {
                                fallback { text("<3") }
                                color(NamedTextColor.RED)
                            }
                        }

                    MiniMessageToDslWriter.write(display) shouldBe
                            """
                    component {
                        display(sprite(key("minecraft", "icon/heart"))) {
                            fallback {
                                text("<3")
                            }
                            color(NamedTextColor.RED)
                        }
                    }
                    """.trimIndent()
                }
            }

            context("gradient emission") {
                test("expands a gradient into per-character coloured children, loss-free") {
                    val input = "<gradient:#ff0000:#0000ff>Hi"
                    val expectedSource =
                        """
                    component {
                        text {
                            text("H") {
                                color(TextColor.color(0xFF0000))
                            }
                            text("i") {
                                color(TextColor.color(0x0000FF))
                            }
                        }
                    }
                    """.trimIndent()

                    // The parser expands the gradient into one coloured child per character before the converter sees
                    // it, so the generated DSL reproduces those children verbatim rather than rebuilding a `gradient`
                    // call. A serialised round-trip would *not* match here: MiniMessage re-compresses the children back
                    // into a single `<gradient>` tag, which is exactly the higher-level form this converter leaves
                    // expanded (lossy-but-faithful, by design).
                    miniToDsl(input) shouldBe expectedSource

                    val gradient = mini(input).childAt(0)
                    gradient shouldHaveChildCount 2
                    gradient.childAt(0) shouldContainText "H"
                    gradient.childAt(0) shouldHaveColor TextColor.color(0xFF0000)
                    gradient.childAt(1) shouldContainText "i"
                    gradient.childAt(1) shouldHaveColor TextColor.color(0x0000FF)
                }
            }

            context("unsupported input") {
                test("rejects unsupported shadow colours instead of dropping them") {
                    val error =
                        shouldThrow<IllegalArgumentException> {
                            miniToDsl("<shadow:red>shadow</shadow>")
                        }

                    error.message shouldContain "miniToDsl cannot represent shadow colours"
                }

                test("rejects unsupported styles nested in children") {
                    val nested =
                        Component
                            .text("ok")
                            .append(Component.text("bad").shadowColor(ShadowColor.shadowColor(0xFF112233.toInt())))

                    val error =
                        shouldThrow<IllegalArgumentException> {
                            MiniMessageToDslWriter.write(nested)
                        }

                    error.message shouldContain "shadow colours"
                }

                test("rejects player-head object contents that have no DSL surface") {
                    val playerHead =
                        Component
                            .`object`()
                            .contents(
                                ObjectContents.playerHead(UUID.fromString("0d1630e2-fc7c-48ef-b7a0-8dfb9e57ec25")),
                            ).build()

                    val error =
                        shouldThrow<IllegalArgumentException> {
                            MiniMessageToDslWriter.write(playerHead)
                        }

                    error.message shouldContain "only sprite contents are supported"
                }

                test("rejects score components with a fixed value instead of dropping it") {
                    val score = Component.score("player", "objective").value("7")

                    val error =
                        shouldThrow<IllegalArgumentException> {
                            MiniMessageToDslWriter.write(score)
                        }

                    error.message shouldContain "score components with a fixed value"
                }

                test("rejects non-component translatable arguments") {
                    val translatable =
                        Component
                            .translatable()
                            .key("stat.generic")
                            .arguments(TranslationArgument.bool(true))
                            .build()

                    val error =
                        shouldThrow<IllegalArgumentException> {
                            MiniMessageToDslWriter.write(translatable)
                        }

                    error.message shouldContain "non-component translatable arguments"
                }

                test("rejects unsupported styles nested in translatable arguments") {
                    val translatable =
                        Component
                            .translatable()
                            .key("chat.type.text")
                            .arguments(Component.text("Alex").shadowColor(ShadowColor.shadowColor(0xFF112233.toInt())))
                            .build()

                    val error =
                        shouldThrow<IllegalArgumentException> {
                            MiniMessageToDslWriter.write(translatable)
                        }

                    error.message shouldContain "shadow colours"
                }

                test("rejects unsupported styles nested in selector separators") {
                    val separator = Component.text(", ").shadowColor(ShadowColor.shadowColor(0xFF112233.toInt()))
                    val selector = Component.selector("@e").separator(separator)

                    val error =
                        shouldThrow<IllegalArgumentException> {
                            MiniMessageToDslWriter.write(selector)
                        }

                    error.message shouldContain "shadow colours"
                }

                test("rejects unsupported styles nested in hover text payloads") {
                    val payload = Component.text("tip").shadowColor(ShadowColor.shadowColor(0xFF112233.toInt()))
                    val component = Component.text("hover me").hoverEvent(HoverEvent.showText(payload))

                    val error =
                        shouldThrow<IllegalArgumentException> {
                            MiniMessageToDslWriter.write(component)
                        }

                    error.message shouldContain "shadow colours"
                }

                test("rejects legacy show-item NBT payloads") {
                    val legacyItem =
                        Component
                            .text("Loot")
                            .hoverEvent(
                                HoverEvent.showItem(
                                    key("minecraft", "diamond_sword"),
                                    1,
                                    BinaryTagHolder.binaryTagHolder("{}"),
                                ),
                            )

                    val error =
                        shouldThrow<IllegalArgumentException> {
                            MiniMessageToDslWriter.write(legacyItem)
                        }

                    error.message shouldContain "legacy show-item NBT"
                }

                test("rejects unsupported data component values") {
                    val unsupportedValue: DataComponentValue = object : DataComponentValue {}
                    val component =
                        component {
                            text("Loot") {
                                hover {
                                    item(
                                        key = key("minecraft", "diamond_sword"),
                                        dataComponents = mapOf(key("minecraft", "custom_data") to unsupportedValue),
                                    )
                                }
                            }
                        }

                    val error =
                        shouldThrow<IllegalArgumentException> {
                            MiniMessageToDslWriter.write(component)
                        }

                    error.message shouldContain "data component value"
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

private fun assertGoldenRoundTrip(
    expectedSource: String,
    expectedComponent: Component,
) {
    val input = MiniMessage.miniMessage().serialize(expectedComponent)

    assertGoldenRoundTrip(input, expectedSource, expectedComponent)
}
