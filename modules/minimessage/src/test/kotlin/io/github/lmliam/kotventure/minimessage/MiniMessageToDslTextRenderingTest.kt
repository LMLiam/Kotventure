package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.text.text
import io.github.lmliam.kotventure.minimessage.conversion.MiniMessageToDslWriter
import io.github.lmliam.kotventure.test.text.childAt
import io.github.lmliam.kotventure.test.text.shouldContainText
import io.github.lmliam.kotventure.test.text.shouldHaveChildCount
import io.github.lmliam.kotventure.test.text.shouldHaveColor
import io.github.lmliam.kotventure.test.text.shouldHaveDecoration
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.ShadowColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

class MiniMessageToDslTextRenderingTest :
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

            context("gradient emission") {
                test("expands a gradient into per-character coloured children (lossy-but-faithful)") {
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

                    miniToDsl(input) shouldBe expectedSource

                    val gradient = mini(input).childAt(0)
                    gradient shouldHaveChildCount 2
                    gradient.childAt(0) shouldContainText "H"
                    gradient.childAt(0) shouldHaveColor TextColor.color(0xFF0000)
                    gradient.childAt(1) shouldContainText "i"
                    gradient.childAt(1) shouldHaveColor TextColor.color(0x0000FF)
                }
            }

            context("shadow emission") {
                test("emits a shadow colour from the <shadow> tag instead of dropping it") {
                    miniToDsl("<shadow:#112233>shadow</shadow>") shouldBe
                        """
                        component {
                            text("shadow") {
                                shadow(ShadowColor.shadowColor(0x3F112233.toInt()))
                            }
                        }
                    """.trimIndent()
                }

                test("emits shadow colours nested in children") {
                    val nested =
                        Component
                            .text("ok")
                            .append(Component.text("bad").shadowColor(ShadowColor.shadowColor(0xFF112233.toInt())))

                    MiniMessageToDslWriter.write(nested) shouldBe
                        """
                        component {
                            text("ok") {
                                text("bad") {
                                    shadow(ShadowColor.shadowColor(0xFF112233.toInt()))
                                }
                            }
                        }
                        """.trimIndent()
                }

                test("emits shadow colours nested in translatable arguments") {
                    val translatable =
                        Component
                            .translatable()
                            .key("chat.type.text")
                            .arguments(Component.text("Alex").shadowColor(ShadowColor.shadowColor(0xFF112233.toInt())))
                            .build()

                    MiniMessageToDslWriter.write(translatable) shouldBe
                        """
                        component {
                            translatable("chat.type.text") {
                                arg {
                                    text("Alex") {
                                        shadow(ShadowColor.shadowColor(0xFF112233.toInt()))
                                    }
                                }
                            }
                        }
                        """.trimIndent()
                }

                test("emits shadow colours nested in selector separators") {
                    val separator = Component.text(", ").shadowColor(ShadowColor.shadowColor(0xFF112233.toInt()))
                    val selector = Component.selector("@e").separator(separator)

                    MiniMessageToDslWriter.write(selector) shouldBe
                        """
                        component {
                            selector("@e") {
                                separator {
                                    text(", ") {
                                        shadow(ShadowColor.shadowColor(0xFF112233.toInt()))
                                    }
                                }
                            }
                        }
                        """.trimIndent()
                }

                test("emits shadow colours nested in hover text payloads") {
                    val payload = Component.text("tip").shadowColor(ShadowColor.shadowColor(0xFF112233.toInt()))
                    val component = Component.text("hover me").hoverEvent(HoverEvent.showText(payload))

                    MiniMessageToDslWriter.write(component) shouldBe
                        """
                        component {
                            text("hover me") {
                                hover {
                                    text {
                                        text("tip") {
                                            shadow(ShadowColor.shadowColor(0xFF112233.toInt()))
                                        }
                                    }
                                }
                            }
                        }
                        """.trimIndent()
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
