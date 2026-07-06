package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.selector.shouldRenderAs
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

class EntitySelectorMapTest :
    StringSpec(
        {
            "scores render exact open closed and negative ranges in declaration order" {
                entities {
                    scores {
                        "kills" eq exactly(5)
                        "level_up" eq atLeast(1)
                        "deaths" eq atMost(3)
                        "balance" eq -10..-1
                        "progress" eq atLeast(-5)
                    }
                } shouldRenderAs "@e[scores={kills=5,level_up=1..,deaths=..3,balance=-10..-1,progress=-5..}]"
            }

            "an exact closed score range collapses to the exact form" {
                allPlayers { scores { "kills" eq 5..5 } } shouldRenderAs "@a[scores={kills=5}]"
            }

            "an empty scores block renders an empty map" {
                entities { scores {} } shouldRenderAs "@e[scores={}]"
            }

            "scores are available on the self scope" {
                self { scores { "kills" eq exactly(1) } } shouldRenderAs "@s[scores={kills=1}]"
            }

            "repeated score objectives are rejected" {
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        scores {
                            "kills" eq atLeast(10)
                            "kills" eq exactly(5)
                        }
                    }
                }
            }

            "a duplicate scores block is rejected" {
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        scores { "kills" eq exactly(5) }
                        scores { "deaths" eq exactly(0) }
                    }
                }
            }

            "score objectives may use every allowed unquoted-token punctuation class" {
                entities { scores { "obj_1.kills-total+x" eq exactly(1) } } shouldRenderAs
                        "@e[scores={obj_1.kills-total+x=1}]"
            }

            "a scores block cannot be negated" {
                assertDoesNotCompile(
                    "NegatedScoresTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun negatedScores() {
                        entities {
                            !scores { "kills" eq exactly(5) }
                        }
                    }
                    """.trimIndent(),
                    "receiver type mismatch",
                )
            }

            "score objectives outside vanilla's unquoted-token syntax are rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { scores { "bad name" eq exactly(1) } }
                }
                shouldThrow<IllegalArgumentException> {
                    entities { scores { "" eq exactly(1) } }
                }
            }

            "a score with an inverted IntRange is rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { scores { "kills" eq 5..1 } }
                }
            }

            "advancements render completion and criterion conditions in declaration order" {
                allPlayers {
                    advancements {
                        key("minecraft", "story/smelt_iron") eq true
                        key("my_pack", "boss") eq {
                            "kill_dragon" eq true
                            "no_deaths" eq false
                        }
                        key("my_pack", "secret") eq false
                    }
                } shouldRenderAs
                        "@a[advancements={minecraft:story/smelt_iron=true," +
                        "my_pack:boss={kill_dragon=true,no_deaths=false},my_pack:secret=false}]"
            }

            "an empty advancement criterion block renders the valid vanilla form" {
                allPlayers { advancements { key("my_pack", "boss") eq {} } } shouldRenderAs
                        "@a[advancements={my_pack:boss={}}]"
            }

            "an empty advancements block renders an empty map" {
                entities { advancements {} } shouldRenderAs "@e[advancements={}]"
            }

            "advancements are available on the self scope" {
                self { advancements { key("minecraft", "story/root") eq true } } shouldRenderAs
                        "@s[advancements={minecraft:story/root=true}]"
            }

            "repeated advancements are rejected" {
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        advancements {
                            key("my_pack", "boss") eq true
                            key("my_pack", "boss") eq { "kill_dragon" eq true }
                        }
                    }
                }
            }

            "a duplicate advancements block is rejected" {
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        advancements { key("my_pack", "boss") eq true }
                        advancements { key("my_pack", "secret") eq false }
                    }
                }
            }

            "repeated advancement criteria are rejected" {
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        advancements {
                            key("my_pack", "boss") eq {
                                "kill_dragon" eq true
                                "kill_dragon" eq false
                            }
                        }
                    }
                }
            }

            "advancement criteria outside vanilla's unquoted-token syntax are rejected" {
                shouldThrow<IllegalArgumentException> {
                    allPlayers { advancements { key("my_pack", "boss") eq { "bad name" eq true } } }
                }
                shouldThrow<IllegalArgumentException> {
                    allPlayers { advancements { key("my_pack", "boss") eq { "" eq true } } }
                }
            }

            "an advancements block cannot be negated" {
                assertDoesNotCompile(
                    "NegatedAdvancementsTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.key.key
                    import io.github.lmliam.kotventure.core.selector.*

                    fun negatedAdvancements() {
                        entities {
                            !advancements { key("my_pack", "boss") eq true }
                        }
                    }
                    """.trimIndent(),
                    "receiver type mismatch",
                )
            }
        },
    )
