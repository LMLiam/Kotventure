package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.selector.shouldRenderAs
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec

class EntitySelectorNegationTest :
    StringSpec(
        {
            "gamemode filter with typed constant" {
                allPlayers { gamemode(survival) } shouldRenderAs "@a[gamemode=survival]"
            }

            "gamemode with all constant variants" {
                allPlayers { gamemode(survival) } shouldRenderAs "@a[gamemode=survival]"
                allPlayers { gamemode(creative) } shouldRenderAs "@a[gamemode=creative]"
                allPlayers { gamemode(adventure) } shouldRenderAs "@a[gamemode=adventure]"
                allPlayers { gamemode(spectator) } shouldRenderAs "@a[gamemode=spectator]"
            }

            "negated filters accumulate in call order" {
                entities {
                    !type(key("minecraft", "zombie"))
                    !typeTag(key("minecraft", "raiders"))
                    !name("Boss")
                    !name("Boss Mob")
                    !gamemode(survival)
                    !gamemode(creative)
                } shouldRenderAs
                        "@e[type=!minecraft:zombie,type=!#minecraft:raiders,name=!Boss,name=!\"Boss Mob\"," +
                        "gamemode=!survival,gamemode=!creative]"
            }

            "entity type tags use Adventure keys and preserve custom namespaces" {
                entities {
                    typeTag(key("mymod", "hostile"))
                } shouldRenderAs "@e[type=#mymod:hostile]"

                self {
                    !typeTag(key("mymod", "ignored"))
                } shouldRenderAs "@s[type=!#mymod:ignored]"
            }

            "negated string types apply the default namespace" {
                entities {
                    !type("creeper")
                } shouldRenderAs "@e[type=!minecraft:creeper]"
            }

            "duplicate positive singleton filters are rejected" {
                shouldThrow<IllegalStateException> {
                    entities {
                        type("zombie")
                        type("skeleton")
                    }
                }
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        name("Alex")
                        name("Steve")
                    }
                }
            }

            "mixed filter polarity is rejected in both orders" {
                shouldThrow<IllegalStateException> {
                    entities {
                        type("zombie")
                        !type("skeleton")
                    }
                }
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        !gamemode(survival)
                        gamemode(creative)
                    }
                }
            }

            "the exclusion operator is not available outside selector scopes" {
                assertDoesNotCompile(
                    "UnscopedExclusionOperatorTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    lateinit var expression: SelectorFilterExpression
                    val excluded = !expression
                    """.trimIndent(),
                    "Unresolved reference 'not'",
                )
            }

            "presence filters cannot be prefix-negated" {
                assertDoesNotCompile(
                    "NegatedPresenceFilterTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidPresenceFilters() {
                        entities {
                            !tag(any)
                            !team(none)
                        }
                    }
                    """.trimIndent(),
                    "receiver type mismatch",
                )
            }

            "value-wrapped negation no longer compiles" {
                assertDoesNotCompile(
                    "ValueWrappedNegationTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun oldNegationSyntax() {
                        entities {
                            tag(!"hidden")
                            type(!"zombie")
                        }
                    }
                    """.trimIndent(),
                    "Argument type mismatch",
                )
            }

            "filter expressions cannot cross selector blocks" {
                lateinit var expression: SelectorFilterExpression
                entities {
                    expression = tag("hidden")
                }

                shouldThrow<IllegalStateException> {
                    entities {
                        !expression
                    }
                }
            }

            "filter expressions cannot be negated twice" {
                shouldThrow<IllegalStateException> {
                    entities {
                        val expression = tag("hidden")
                        !expression
                        !expression
                    }
                }
            }

            "predicate filters preserve positive and negated keys in call order" {
                entities {
                    predicate(key("minecraft", "is_baby"))
                    !predicate(key("my_pack", "hidden"))
                    predicate(key("my_pack", "active"))
                } shouldRenderAs
                        "@e[predicate=minecraft:is_baby,predicate=!my_pack:hidden,predicate=my_pack:active]"
            }

            "identical predicates accumulate rather than collapse" {
                entities {
                    predicate(key("my_pack", "active"))
                    predicate(key("my_pack", "active"))
                } shouldRenderAs "@e[predicate=my_pack:active,predicate=my_pack:active]"
            }

            "predicates are available on the self scope" {
                self { !predicate(key("my_pack", "flying")) } shouldRenderAs "@s[predicate=!my_pack:flying]"
            }

            "raw predicate strings do not compile" {
                assertDoesNotCompile(
                    "RawPredicateTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun rawPredicate() {
                        entities {
                            predicate("my_pack:active")
                        }
                    }
                    """.trimIndent(),
                    "Argument type mismatch",
                )
            }
        },
    )
