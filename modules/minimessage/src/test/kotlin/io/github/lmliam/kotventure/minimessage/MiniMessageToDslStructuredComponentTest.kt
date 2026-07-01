package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.green
import io.github.lmliam.kotventure.core.color.red
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.keybind.keybind
import io.github.lmliam.kotventure.core.nbt.blockNbt
import io.github.lmliam.kotventure.core.nbt.blockPos
import io.github.lmliam.kotventure.core.nbt.entityNbt
import io.github.lmliam.kotventure.core.nbt.nbtPath
import io.github.lmliam.kotventure.core.nbt.storageNbt
import io.github.lmliam.kotventure.core.objectcomponent.display
import io.github.lmliam.kotventure.core.objectcomponent.sprite
import io.github.lmliam.kotventure.core.score.score
import io.github.lmliam.kotventure.core.selector.entitySelector
import io.github.lmliam.kotventure.core.selector.selector
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.translatable.translatable
import io.github.lmliam.kotventure.minimessage.conversion.MiniMessageToDslConversionException
import io.github.lmliam.kotventure.minimessage.conversion.MiniMessageToDslWriter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslationArgument
import net.kyori.adventure.text.event.DataComponentValue
import net.kyori.adventure.text.`object`.ObjectContents
import java.util.UUID

class MiniMessageToDslStructuredComponentTest :
    FunSpec(
        {
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
                                color(green)
                            }
                        }
                        """.trimIndent(),
                        expectedComponent =
                            component {
                                keybind("key.sneak") {
                                    color(green)
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
                            selector(nearestPlayer())
                        }
                        """.trimIndent(),
                        expectedComponent = component { selector(entitySelector("@p")) },
                    )
                }

                test("round-trips selector separators against compiled expected DSL") {
                    assertGoldenRoundTrip(
                        input = "<selector:'@e':', '>",
                        expectedSource =
                            """
                        component {
                            selector(entities()) {
                                separator {
                                    text(", ")
                                }
                            }
                        }
                        """.trimIndent(),
                        expectedComponent =
                            component {
                                selector(entitySelector("@e")) {
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
                                        color(red)
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
                                            color(red)
                                            text("y") { bold() }
                                        }
                                    }
                                }
                            },
                    )
                }

                test("emits boolean and numeric translatable arguments through the arg overloads") {
                    val translatable =
                        Component
                            .translatable()
                            .key("stat.generic")
                            .arguments(TranslationArgument.bool(true), TranslationArgument.numeric(42))
                            .build()

                    MiniMessageToDslWriter.write(translatable) shouldBe
                            """
                    component {
                        translatable("stat.generic") {
                            arg(true)
                            arg(42)
                        }
                    }
                    """.trimIndent()
                }

                test("preserves the numeric type of translatable arguments in generated source") {
                    val translatable =
                        Component
                            .translatable()
                            .key("stat.generic")
                            .arguments(
                                TranslationArgument.numeric(42L),
                                TranslationArgument.numeric(1.5f),
                                TranslationArgument.numeric(2.5),
                            ).build()

                    MiniMessageToDslWriter.write(translatable) shouldBe
                            """
                    component {
                        translatable("stat.generic") {
                            arg(42L)
                            arg(1.5f)
                            arg(2.5)
                        }
                    }
                    """.trimIndent()
                }

                test("emits Long.MIN_VALUE as a constant rather than an uncompilable literal") {
                    val translatable =
                        Component
                            .translatable()
                            .key("stat.generic")
                            .arguments(TranslationArgument.numeric(Long.MIN_VALUE))
                            .build()

                    MiniMessageToDslWriter.write(translatable) shouldBe
                            """
                    component {
                        translatable("stat.generic") {
                            arg(Long.MIN_VALUE)
                        }
                    }
                    """.trimIndent()
                }

                test("emits non-finite numeric translatable arguments as qualified constants") {
                    val translatable =
                        Component
                            .translatable()
                            .key("stat.generic")
                            .arguments(
                                TranslationArgument.numeric(Double.NaN),
                                TranslationArgument.numeric(Float.POSITIVE_INFINITY),
                            ).build()

                    MiniMessageToDslWriter.write(translatable) shouldBe
                            """
                    component {
                        translatable("stat.generic") {
                            arg(Double.NaN)
                            arg(Float.POSITIVE_INFINITY)
                        }
                    }
                    """.trimIndent()
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
                                color(green)
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
                            color(green)
                            text("!")
                        }
                    }
                    """.trimIndent()
                }
            }

            context("NBT component emission") {
                test("emits bare block NBT components from a compiled expected DSL") {
                    val nbt = component { blockNbt(blockPos("1 64 -3"), nbtPath("Items")) }

                    MiniMessageToDslWriter.write(nbt) shouldBe
                            """
                    component {
                        blockNbt(blockPos("1 64 -3"), nbtPath("Items"))
                    }
                    """.trimIndent()
                }

                test("emits block NBT interpretation, separators, and style together") {
                    val nbt =
                        component {
                            blockNbt(blockPos("1 64 -3"), nbtPath("Items")) {
                                interpret(true)
                                separator { text(", ") }
                                color(aqua)
                            }
                        }

                    MiniMessageToDslWriter.write(nbt) shouldBe
                            """
                    component {
                        blockNbt(blockPos("1 64 -3"), nbtPath("Items")) {
                            interpret(true)
                            separator {
                                text(", ")
                            }
                            color(aqua)
                        }
                    }
                    """.trimIndent()
                }

                test("emits entity NBT components from a compiled expected DSL") {
                    val nbt = component { entityNbt(entitySelector("@e[type=armor_stand]"), nbtPath("Pos")) }

                    MiniMessageToDslWriter.write(nbt) shouldBe
                            """
                    component {
                        entityNbt(entitySelector("@e[type=armor_stand]"), nbtPath("Pos"))
                    }
                    """.trimIndent()
                }

                test("emits storage NBT components carrying interpretation") {
                    val nbt =
                        component {
                            storageNbt(key("minecraft", "data"), nbtPath("Contents")) {
                                interpret(true)
                            }
                        }

                    MiniMessageToDslWriter.write(nbt) shouldBe
                            """
                    component {
                        storageNbt(key("minecraft", "data"), nbtPath("Contents")) {
                            interpret(true)
                        }
                    }
                    """.trimIndent()
                }

                test("round-trips block NBT from MiniMessage input through the full parse-write path") {
                    assertGoldenRoundTrip(
                        input = "<nbt:block:'1 64 -3':Items>",
                        expectedSource =
                            """
                        component {
                            blockNbt(blockPos("1 64 -3"), nbtPath("Items"))
                        }
                        """.trimIndent(),
                        expectedComponent = component { blockNbt(blockPos("1 64 -3"), nbtPath("Items")) },
                    )
                }

                test("round-trips selector with arguments through the full parse-write path") {
                    assertGoldenRoundTrip(
                        input = "<selector:'@e[type=armor_stand,limit=1]'>",
                        expectedSource =
                            """
                        component {
                            selector(entitySelector("@e[type=armor_stand,limit=1]"))
                        }
                        """.trimIndent(),
                        expectedComponent =
                            component { selector(entitySelector("@e[type=armor_stand,limit=1]")) },
                    )
                }

                test("round-trips complex NBT path through the raw wrapper") {
                    assertGoldenRoundTrip(
                        input = "<nbt:block:'1 64 -3':'Items[0].tag.display.Name'>",
                        expectedSource =
                            """
                        component {
                            blockNbt(blockPos("1 64 -3"), nbtPath("Items[0].tag.display.Name"))
                        }
                        """.trimIndent(),
                        expectedComponent =
                            component {
                                blockNbt(blockPos("1 64 -3"), nbtPath("Items[0].tag.display.Name"))
                            },
                    )
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
                                color(red)
                            }
                        }

                    MiniMessageToDslWriter.write(display) shouldBe
                            """
                    component {
                        display(sprite(key("minecraft", "icon/heart"))) {
                            fallback {
                                text("<3")
                            }
                            color(red)
                        }
                    }
                    """.trimIndent()
                }

                test("round-trips object sprites from MiniMessage input through the full parse-write path") {
                    assertGoldenRoundTrip(
                        input = "<sprite:icon/heart>",
                        expectedSource =
                            """
                        component {
                            display(sprite(key("minecraft", "icon/heart")))
                        }
                        """.trimIndent(),
                        expectedComponent = component { display(sprite(key("minecraft", "icon/heart"))) },
                    )
                }
            }

            context("player-head emission") {
                test("emits a named player head from the <head> tag instead of dropping it") {
                    miniToDsl("<head:Steve>") shouldBe
                            """
                    component {
                        display(head("Steve"))
                    }
                    """.trimIndent()
                }

                test("emits a player head from a uuid") {
                    val playerHead =
                        Component
                            .`object`()
                            .contents(
                                ObjectContents.playerHead(UUID.fromString("0d1630e2-fc7c-48ef-b7a0-8dfb9e57ec25")),
                            ).build()

                    MiniMessageToDslWriter.write(playerHead) shouldBe
                            """
                        component {
                            display(head(uuid("0d1630e2-fc7c-48ef-b7a0-8dfb9e57ec25")))
                        }
                        """.trimIndent()
                }

                test("emits a player head texture key and hat toggle") {
                    val contents =
                        ObjectContents
                            .playerHead()
                            .texture(Key.key("minecraft", "entity/player/wide/steve"))
                            .hat(false)
                            .build()
                    val playerHead = Component.`object`().contents(contents).build()

                    MiniMessageToDslWriter.write(playerHead) shouldBe
                            """
                        component {
                            display(head(key("minecraft", "entity/player/wide/steve"), hat = false))
                        }
                        """.trimIndent()
                }
            }

            context("unsupported input") {
                test("rejects a player head with no single skin source") {
                    val playerHead = Component.`object`().contents(ObjectContents.playerHead().build()).build()

                    shouldThrow<MiniMessageToDslConversionException> {
                        MiniMessageToDslWriter.write(playerHead)
                    }
                }

                test("rejects unsupported data component values") {
                    val unsupportedValue: DataComponentValue = object : DataComponentValue {}
                    val component =
                        component {
                            text("Loot") {
                                hover {
                                    item(key("minecraft", "diamond_sword")) {
                                        component(key("minecraft", "custom_data"), unsupportedValue)
                                    }
                                }
                            }
                        }

                    shouldThrow<MiniMessageToDslConversionException> {
                        MiniMessageToDslWriter.write(component)
                    }
                }
            }
        },
    )
