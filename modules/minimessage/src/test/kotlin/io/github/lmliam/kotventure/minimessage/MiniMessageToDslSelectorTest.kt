package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.selector.entitySelector
import io.github.lmliam.kotventure.core.selector.selector
import io.github.lmliam.kotventure.minimessage.conversion.MiniMessageToDslWriter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage

class MiniMessageToDslSelectorTest :
    FunSpec(
        {
            test("emits every typed selector factory") {
                mapOf(
                    "@p" to "nearestPlayer()",
                    "@a" to "allPlayers()",
                    "@r" to "randomPlayer()",
                    "@s" to "self()",
                    "@e" to "entities()",
                    "@n" to "nearestEntity()",
                ).forEach { (pattern, factory) ->
                    assertSelectorRoundTrip(
                        pattern = pattern,
                        expectedSelectorSource = factory,
                    )
                }
            }

            test("emits typed selectors through the MiniMessage parse and conversion path") {
                assertGoldenRoundTrip(
                    input = "<selector:'@e[type=!minecraft:zombie,limit=2]'>",
                    expectedSource =
                        """
                        component {
                            selector(
                                entities {
                                    not {
                                        type(key("minecraft", "zombie"))
                                    }
                                    limit(2)
                                }
                            )
                        }
                        """.trimIndent(),
                    expectedComponent =
                        component {
                            selector(entitySelector("@e[type=!minecraft:zombie,limit=2]"))
                        },
                )
            }

            test("emits every typed selector argument including nested structures and negation") {
                assertSelectorRoundTrip(
                    pattern =
                        "@e[" +
                            "type=!minecraft:skeleton,type=!#minecraft:undead," +
                            "name=!Bot," +
                            "x=1,y=2,z=3,dx=4,dy=5,dz=6," +
                            "distance=1..10,x_rotation=..45,y_rotation=-180..180," +
                            "level=1..30,gamemode=!creative," +
                            "limit=5,sort=nearest," +
                            "tag=visible,tag=!hidden,tag=!,tag=," +
                            "team=!red,team=blue," +
                            "nbt={Health:20.0f,Tags:[\"boss\"]},nbt=!{Invisible:1b}," +
                            "scores={kills=10..,deaths=..2}," +
                            "predicate=minecraft:visible,predicate=!minecraft:hidden," +
                            "advancements={" +
                            "minecraft:story/root=true," +
                            "minecraft:story/mine_diamond={diamond=false}" +
                            "}]",
                    expectedSelectorSource =
                        """
                        entities {
                            not {
                                type(key("minecraft", "skeleton"))
                                typeTag(key("minecraft", "undead"))
                                name("Bot")
                            }
                            origin(x = 1.0, y = 2.0, z = 3.0)
                            volume(dx = 4.0, dy = 5.0, dz = 6.0)
                            distance(1.0..10.0)
                            xRotation(atMost(45.0))
                            yRotation(-180.0..180.0)
                            level(1..30)
                            not {
                                gamemode(creative)
                            }
                            limit(5)
                            sort(nearest)
                            tag("visible")
                            not {
                                tag("hidden")
                            }
                            tag(any)
                            tag(none)
                            not {
                                team("red")
                            }
                            team("blue")
                            nbt {
                                "Health" eq 20.0f
                                "Tags" eq list("boss")
                            }
                            not {
                                nbt {
                                    "Invisible" eq 1.toByte()
                                }
                            }
                            score("kills", atLeast(10))
                            score("deaths", atMost(2))
                            predicate(key("minecraft", "visible"))
                            not {
                                predicate(key("minecraft", "hidden"))
                            }
                            advancement(key("minecraft", "story/root"), completed = true)
                            advancement(key("minecraft", "story/mine_diamond")) {
                                criterion("diamond", completed = false)
                            }
                        }
                        """.trimIndent(),
                )
            }

            test("emits positive type name and game mode filters") {
                assertSelectorRoundTrip(
                    pattern = "@s[type=minecraft:player,name=\"Boss Mob\",gamemode=survival]",
                    expectedSelectorSource =
                        """
                        self {
                            type(key("minecraft", "player"))
                            name("Boss Mob")
                            gamemode(survival)
                        }
                        """.trimIndent(),
                )
            }

            test("emits exact floating point level and score ranges") {
                assertSelectorRoundTrip(
                    pattern = "@e[distance=5,x_rotation=0,y_rotation=90,level=2,scores={kills=1}]",
                    expectedSelectorSource =
                        """
                        entities {
                            distance(exactly(5.0))
                            xRotation(exactly(0.0))
                            yRotation(exactly(90.0))
                            level(exactly(2))
                            score("kills", exactly(1))
                        }
                        """.trimIndent(),
                )
            }

            test("falls back losslessly for unsupported or non-canonical selector syntax") {
                listOf(
                    "@e[future_argument=1]",
                    "@e[type=armor_stand,limit=1]",
                    "@e[limit=1,type=minecraft:zombie]",
                    "@e[name='Boss Mob']",
                    "@e[]",
                    "@e[scores={kills=1},scores={deaths=2}]",
                    "@e[scores={}]",
                    "@e[advancements={}]",
                    "@e[nbt={Items:[]}]",
                    "@e[type=minecraft:zombie,type=!minecraft:skeleton]",
                    "@e[name=Alex,name=!Bot]",
                    "@e[gamemode=survival,gamemode=!creative]",
                    "@e[x=1.0]",
                    "@e[future=\"say \\\\\"hi\\\\\"\"]",
                    "@e[team=!,team=!red]",
                    "@e[future=\"a\b\u000cb\u0001\"]",
                ).forEach { pattern ->
                    assertSelectorRoundTrip(
                        pattern = pattern,
                        expectedSelectorSource = "entitySelector(\"${pattern.escapeForExpectedKotlin()}\")",
                    )
                }
            }
        },
    )

private fun assertSelectorRoundTrip(
    pattern: String,
    expectedSelectorSource: String,
) {
    val component = Component.selector(pattern)
    val expectedSource =
        if ('\n' in expectedSelectorSource) {
            listOf(
                "component {",
                "    selector(",
                expectedSelectorSource.prependIndent("        "),
                "    )",
                "}",
            ).joinToString("\n")
        } else {
            """
            component {
                selector($expectedSelectorSource)
            }
            """.trimIndent()
        }
    val generated = MiniMessageToDslWriter.write(component)

    generated shouldBe expectedSource
    MiniMessage.miniMessage().serialize(compileGeneratedDsl(generated)) shouldBe
        MiniMessage.miniMessage().serialize(component)
}

private fun String.escapeForExpectedKotlin(): String =
    buildString {
        this@escapeForExpectedKotlin.forEach { character ->
            when (character) {
                '\\' -> append("\\\\")
                '"' -> append("\\\"")
                '\b' -> append("\\b")
                '\u000C' -> append("\\u000c")
                else ->
                    if (character.code < 0x20 || character.code == 0x7F) {
                        append("\\u%04x".format(character.code))
                    } else {
                        append(character)
                    }
            }
        }
    }
