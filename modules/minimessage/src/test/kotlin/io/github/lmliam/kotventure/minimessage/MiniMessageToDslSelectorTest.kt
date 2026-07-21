package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.minimessage.conversion.MiniMessageToDslWriter
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.text.Component

class MiniMessageToDslSelectorTest :
    FunSpec(
        {
            fun writeSelector(pattern: String): String = MiniMessageToDslWriter.write(Component.selector(pattern))

            context("typed selector emission") {
                test("emits every selector head as its factory") {
                    mapOf(
                        "@p" to "nearestPlayer()",
                        "@a" to "allPlayers()",
                        "@r" to "randomPlayer()",
                        "@s" to "self()",
                        "@e" to "entities()",
                        "@n" to "nearestEntity()",
                    ).forEach { (pattern, factory) ->
                        writeSelector(pattern) shouldBe
                                """
                            component {
                                selector(
                                    $factory
                                )
                            }
                            """.trimIndent()
                    }
                }

                test("emits every typed argument in model order") {
                    val pattern =
                        "@e[type=minecraft:zombie,name=\"Boss Mob\",x=12.5,y=64,z=-4,dx=16,dy=8,dz=-16," +
                                "distance=0..64,x_rotation=..45,y_rotation=170..-170,level=0..30,gamemode=survival," +
                                "limit=5,sort=nearest,tag=boss,team=raiders,nbt={Health:20.5f}," +
                                "scores={kills=10..},predicate=minecraft:is_baby,advancements={minecraft:story/root=true}]"

                    writeSelector(pattern) shouldBe
                            """
                        component {
                            selector(
                                entities {
                                    type(key("minecraft", "zombie"))
                                    name("Boss Mob")
                                    origin(12.5.x, 64.0.y, (-4.0).z)
                                    volume(16.0.dx, 8.0.dy, (-16.0).dz)
                                    distance(0.0..64.0)
                                    pitch(atMost(45.0))
                                    yaw(170.0..-170.0)
                                    level(0..30)
                                    gamemode(survival)
                                    limit(5)
                                    sort(nearest)
                                    tag("boss")
                                    team("raiders")
                                    nbt { "Health" eq 20.5f }
                                    scores {
                                        "kills" eq atLeast(10)
                                    }
                                    predicate(key("minecraft", "is_baby"))
                                    advancements {
                                        key("minecraft", "story/root") eq true
                                    }
                                }
                            )
                        }
                        """.trimIndent()
                }

                test("emits negated filters with the scope's negation operator") {
                    val pattern =
                        "@e[type=!minecraft:zombie,type=!#minecraft:undead,name=!Bot,gamemode=!creative," +
                                "tag=!hidden,team=!red,nbt=!{Invisible:1b},predicate=!my_pack:hidden]"

                    writeSelector(pattern) shouldBe
                            """
                        component {
                            selector(
                                entities {
                                    !type(key("minecraft", "zombie"))
                                    !typeTag(key("minecraft", "undead"))
                                    !name("Bot")
                                    !gamemode(creative)
                                    !tag("hidden")
                                    !team("red")
                                    !nbt { "Invisible" eq 1.toByte() }
                                    !predicate(key("my_pack", "hidden"))
                                }
                            )
                        }
                        """.trimIndent()
                }

                test("emits presence filters as scope constants") {
                    writeSelector("@e[tag=,tag=!,team=!]") shouldBe
                            """
                        component {
                            selector(
                                entities {
                                    tag(none)
                                    tag(any)
                                    team(any)
                                }
                            )
                        }
                        """.trimIndent()
                }

                test("emits exact and open-ended ranges through the range builders") {
                    writeSelector("@e[distance=5,x_rotation=-45..,level=3]") shouldBe
                            """
                        component {
                            selector(
                                entities {
                                    distance(exactly(5.0))
                                    pitch(atLeast(-45.0))
                                    level(exactly(3))
                                }
                            )
                        }
                        """.trimIndent()
                }

                test("emits empty compound maps and empty SNBT containers") {
                    writeSelector("@e[nbt={},scores={},advancements={minecraft:story/root={}}]") shouldBe
                            """
                        component {
                            selector(
                                entities {
                                    nbt { }
                                    scores { }
                                    advancements {
                                        key("minecraft", "story/root") eq { }
                                    }
                                }
                            )
                        }
                        """.trimIndent()
                }

                test("emits an empty SNBT list as list()") {
                    writeSelector("@e[nbt={Items:[]}]") shouldBe
                            """
                        component {
                            selector(
                                entities {
                                    nbt { "Items" eq list() }
                                }
                            )
                        }
                        """.trimIndent()
                }

                test("emits advancement criteria as nested eq blocks") {
                    writeSelector("@a[advancements={my_pack:boss={kill_dragon=true,no_deaths=false}}]") shouldBe
                            """
                        component {
                            selector(
                                allPlayers {
                                    advancements {
                                        key("my_pack", "boss") eq {
                                            "kill_dragon" eq true
                                            "no_deaths" eq false
                                        }
                                    }
                                }
                            )
                        }
                        """.trimIndent()
                }

                test("canonicalizes bare entity types with the minecraft namespace") {
                    writeSelector("@e[type=zombie]") shouldBe
                            """
                        component {
                            selector(
                                entities {
                                    type(key("minecraft", "zombie"))
                                }
                            )
                        }
                        """.trimIndent()
                }
            }

            context("typed selector round trips") {
                test("compiles and round-trips a selector exercising every argument") {
                    // Argument order matches the canonical order of the DSL builder.
                    // Thus, the compiled typed output serialises to the same pattern.
                    val pattern =
                        "@e[type=minecraft:zombie,name=\"Boss Mob\",x=12.5,dy=8,distance=0..64," +
                                "x_rotation=..45,level=0..30,scores={kills=10..,deaths=0..5}," +
                                "advancements={my_pack:boss={kill_dragon=true}},gamemode=!survival,team=," +
                                "limit=5,sort=nearest,tag=boss,tag=!,nbt={Health:20.5f,Items:[]}," +
                                "predicate=minecraft:is_baby]"

                    assertGoldenRoundTrip(
                        input = "<selector:'$pattern'>",
                        expectedSource =
                            """
                        component {
                            selector(
                                entities {
                                    type(key("minecraft", "zombie"))
                                    name("Boss Mob")
                                    origin(12.5.x)
                                    volume(8.0.dy)
                                    distance(0.0..64.0)
                                    pitch(atMost(45.0))
                                    level(0..30)
                                    scores {
                                        "kills" eq atLeast(10)
                                        "deaths" eq 0..5
                                    }
                                    advancements {
                                        key("my_pack", "boss") eq {
                                            "kill_dragon" eq true
                                        }
                                    }
                                    !gamemode(survival)
                                    team(none)
                                    limit(5)
                                    sort(nearest)
                                    tag("boss")
                                    tag(any)
                                    nbt { "Health" eq 20.5f; "Items" eq list() }
                                    predicate(key("minecraft", "is_baby"))
                                }
                            )
                        }
                        """.trimIndent(),
                        expectedComponent = Component.selector(pattern),
                    )
                }

                test("compiles and round-trips a typed entity NBT selector") {
                    assertGoldenRoundTrip(
                        input = "<nbt:entity:'@e[type=minecraft:armor_stand,limit=1]':Pos>",
                        expectedSource =
                            """
                        component {
                            entityNbt(
                                entities {
                                    type(key("minecraft", "armor_stand"))
                                    limit(1)
                                },
                                nbtPath("Pos")
                            )
                        }
                        """.trimIndent(),
                        expectedComponent =
                            Component.entityNBT("Pos", "@e[type=minecraft:armor_stand,limit=1]"),
                    )
                }
            }
        },
    )
