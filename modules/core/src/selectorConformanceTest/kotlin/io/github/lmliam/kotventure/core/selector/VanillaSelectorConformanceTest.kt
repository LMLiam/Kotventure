package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.nbt.list
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.string.shouldContain
import net.kyori.adventure.key.Key

class VanillaSelectorConformanceTest :
    StringSpec(
        {
            "accepts all six canonical selector heads" {
                VanillaSelectorGrammar.shouldAccept(
                    listOf(
                        self(),
                        nearestPlayer(),
                        allPlayers(),
                        randomPlayer(),
                        entities(),
                        nearestEntity(),
                    ),
                )
            }

            "accepts every canonical argument emitted by the typed DSL" {
                val allArguments =
                    entities {
                        typeTag(Key.key("minecraft", "raiders"))
                        name("Boss Mob")
                        origin(x = 12.5, y = 64.0, z = -4.0)
                        volume(dx = 16.0, dy = 8.0, dz = -16.0)
                        distance(0.0..64.0)
                        xRotation(atMost(45.0))
                        yRotation(170.0..-170.0)
                        level(0..30)
                        gamemode(survival)
                        limit(5)
                        sort(nearest)
                        tag("boss")
                        team("raiders")
                        nbt { "Tags" eq list("boss", "hostile") }
                        score("kills", atLeast(10))
                        predicate(Key.key("minecraft", "is_baby"))
                        advancement(Key.key("minecraft", "story/root"), completed = true)
                    }

                VanillaSelectorGrammar.shouldAccept(listOf(allArguments))
            }

            "accepts capability-specific output for every selector head" {
                VanillaSelectorGrammar.shouldAccept(
                    listOf(
                        nearestPlayer {
                            sort(nearest)
                            limit(1)
                        },
                        allPlayers {
                            sort(arbitrary)
                            limit(5)
                        },
                        randomPlayer {
                            sort(random)
                            limit(2)
                        },
                        self { type(Key.key("minecraft", "player")) },
                        entities {
                            type(Key.key("minecraft", "zombie"))
                            sort(furthest)
                            limit(3)
                        },
                        nearestEntity {
                            typeTag(Key.key("minecraft", "undead"))
                            limit(1)
                        },
                    ),
                )
            }

            "accepts repeated empty quoted negated and boundary forms" {
                val repeatedFilters =
                    entities {
                        tag(any)
                        tag(none)
                        tag("visible")
                        not {
                            type(Key.key("minecraft", "zombie"))
                            type(Key.key("minecraft", "skeleton"))
                            typeTag(Key.key("minecraft", "undead"))
                            name("Bot")
                            gamemode(creative)
                            gamemode(spectator)
                            tag("hidden")
                            team("red")
                            team("blue")
                            nbt { "Invisible" eq true }
                            predicate(Key.key("my_pack", "hidden"))
                        }
                        team(any)
                        name("Boss \"Mob\"")
                        nbt { "Health" eq 20.0f }
                        predicate(Key.key("my_pack", "visible"))
                        advancement(Key.key("my_pack", "secret")) {
                            criterion("found_item", completed = false)
                        }
                    }
                val numericBoundaries =
                    entities {
                        origin(x = Double.MIN_VALUE, y = Double.MAX_VALUE, z = -Double.MAX_VALUE)
                        distance(atLeast(0.0))
                        xRotation(-180.0..180.0)
                        yRotation(170.0..-170.0)
                        level(0..Int.MAX_VALUE)
                        limit(Int.MAX_VALUE)
                    }

                VanillaSelectorGrammar.shouldAccept(listOf(repeatedFilters, numericBoundaries))
            }

            "rejects intentionally invalid vanilla selectors with useful diagnostics" {
                VanillaSelectorGrammar.shouldReject(
                    "@q",
                    "@s[limit=1]",
                    "@s[sort=nearest]",
                    "@e[distance=-1]",
                    "@e[limit=0]",
                    "@e[type=minecraft:zombie,type=minecraft:skeleton]",
                    "@e[type=!!minecraft:zombie]",
                    "@e[type=Bad:Key]",
                    "@e[unknown=value]",
                    "@e[scores={kills=}]",
                    "@e[advancements={minecraft:story/root=maybe}]",
                    "@e[nbt={id:minecraft:stone}]",
                    "@e[name=\"unterminated]",
                    "@e[] trailing",
                )
            }

            "reports the rejected selector offset and vanilla expectation" {
                val failure =
                    shouldThrow<AssertionError> {
                        VanillaSelectorGrammar.shouldAccept(
                            listOf(entitySelector("@s[limit=1]")),
                        )
                    }

                failure.message shouldContain "@s[limit=1]"
                failure.message shouldContain "offset"
                failure.message shouldContain "Option 'limit' isn't applicable here"
            }
        },
    )
