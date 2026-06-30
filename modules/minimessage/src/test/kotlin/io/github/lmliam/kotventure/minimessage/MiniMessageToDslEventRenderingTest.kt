package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.color.aqua
import io.github.lmliam.kotventure.core.color.gold
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.event.removed
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.nbt.nbt
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.core.uuid.uuid
import io.github.lmliam.kotventure.minimessage.conversion.MiniMessageToDslConversionException
import io.github.lmliam.kotventure.minimessage.conversion.MiniMessageToDslWriter
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.event.ClickEvent

class MiniMessageToDslEventRenderingTest :
    FunSpec(
        {
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

                test("rejects non-representable click actions") {
                    val payload = ClickEvent.Payload.custom(key("kotventure", "claim"))
                    val component =
                        component {
                            text("Claim") {
                                click(ClickEvent.Action.CUSTOM, payload)
                            }
                        }

                    shouldThrow<MiniMessageToDslConversionException> {
                        MiniMessageToDslWriter.write(component)
                    }
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
                                            color(gold)
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
                                                color(gold)
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
                                            key("minecraft", "custom_data") to nbt { "kotventure" eq 1.toByte() }
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
                                                mapOf(
                                                    key("minecraft", "custom_data") to nbt("{kotventure:1b}"),
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
                                            mapOf(
                                                key("minecraft", "damage") to nbt("{value:5b}"),
                                                key("minecraft", "custom_data") to nbt("{kotventure:1b}"),
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
                                        key("minecraft", "custom_data") to nbt { "kotventure" eq 1.toByte() },
                                        key("minecraft", "damage") to nbt { "value" eq 5.toByte() }
                                    )
                                )
                            }
                        }
                    }
                    """.trimIndent()
                }

                test("round-trips negative and minimum NBT scalars as compilable literals") {
                    assertGoldenRoundTrip(
                        expectedSource =
                            """
                        component {
                            text("Edge data") {
                                hover {
                                    item(
                                        key = key("minecraft", "diamond_sword"),
                                        dataComponents = mapOf(
                                            key("minecraft", "custom_data") to nbt { "big" eq Int.MIN_VALUE; "huge" eq Long.MIN_VALUE; "small" eq (-5).toShort(); "tiny" eq (-5).toByte() }
                                        )
                                    )
                                }
                            }
                        }
                        """.trimIndent(),
                        expectedComponent =
                            component {
                                text("Edge data") {
                                    hover {
                                        item(
                                            key = key("minecraft", "diamond_sword"),
                                            dataComponents =
                                                mapOf(
                                                    key("minecraft", "custom_data") to
                                                            nbt(
                                                                "{big:-2147483648,huge:-9223372036854775808L,small:-5s,tiny:-5b}",
                                                            ),
                                                ),
                                        )
                                    }
                                }
                            },
                    )
                }

                test("falls back to raw nbt for data components the DSL can't model losslessly") {
                    assertGoldenRoundTrip(
                        expectedSource =
                            """
                        component {
                            text("Loot data") {
                                hover {
                                    item(
                                        key = key("minecraft", "diamond_sword"),
                                        dataComponents = mapOf(
                                            key("minecraft", "custom_data") to nbt("{items:[1,2,3]}")
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
                                                mapOf(
                                                    key("minecraft", "custom_data") to nbt("{items:[1,2,3]}"),
                                                ),
                                        )
                                    }
                                }
                            },
                    )
                }

                // Asserted via direct write rather than assertGoldenRoundTrip: Adventure's MiniMessage
                // serializer can't emit a `show_item` hover carrying a removed (non-TagSerializable) data
                // component, so the round-trip harness can't reach this emission branch.
                test("emits removed data components as removed()") {
                    val loot =
                        component {
                            text("Loot data") {
                                hover {
                                    item(
                                        key = key("minecraft", "diamond_sword"),
                                        dataComponents =
                                            mapOf(
                                                key("minecraft", "custom_data") to removed(),
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
                                        key("minecraft", "custom_data") to removed()
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
                                        id = uuid("0d1630e2-fc7c-48ef-b7a0-8dfb9e57ec25")
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
                                            id = uuid("0d1630e2-fc7c-48ef-b7a0-8dfb9e57ec25"),
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
                                        id = uuid("3f5f1f4e-29cb-4c98-93f0-3c7f4b52ddee")
                                    ) {
                                        text("Alex \"\$5\"") {
                                            color(aqua)
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
                                            id = uuid("3f5f1f4e-29cb-4c98-93f0-3c7f4b52ddee"),
                                        ) {
                                            text("Alex \"$5\"") {
                                                color(aqua)
                                            }
                                        }
                                    }
                                }
                            },
                    )
                }
            }
        },
    )
