package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.github.lmliam.kotventure.test.selector.shouldRenderAs
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EntitySelectorHeadTest :
    StringSpec(
        {
            "self returns @s" {
                self() shouldRenderAs "@s"
            }

            "nearestPlayer with no arguments returns @p" {
                nearestPlayer() shouldRenderAs "@p"
            }

            "allPlayers with no arguments returns @a" {
                allPlayers() shouldRenderAs "@a"
            }

            "randomPlayer with no arguments returns @r" {
                randomPlayer() shouldRenderAs "@r"
            }

            "entities with no arguments returns @e" {
                entities() shouldRenderAs "@e"
            }

            "nearestEntity with no arguments returns @n" {
                nearestEntity() shouldRenderAs "@n"
            }

            "self accepts common and entity type arguments" {
                self {
                    type("minecraft:zombie")
                    name("Boss")
                } shouldRenderAs "@s[type=minecraft:zombie,name=Boss]"
            }

            "player selector scopes retain limit and sort" {
                nearestPlayer {
                    limit(3)
                    sort(nearest)
                } shouldRenderAs "@p[limit=3,sort=nearest]"
            }

            "player selector scopes do not expose entity type" {
                assertDoesNotCompile(
                    "PlayerSelectorTypeTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidPlayerSelector() {
                        nearestPlayer {
                            type("minecraft:zombie")
                        }
                    }
                    """.trimIndent(),
                    "Unresolved reference 'type'",
                )
            }

            "player selector scopes do not expose negated entity type filters" {
                assertDoesNotCompile(
                    "NegatedPlayerSelectorTypeTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidPlayerSelector() {
                        allPlayers {
                            !type("zombie")
                        }
                    }
                    """.trimIndent(),
                    "Unresolved reference 'type'",
                )
            }

            "self selector scope does not expose limit or sort" {
                assertDoesNotCompile(
                    "SelfSelectorLimitTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidSelfSelector() {
                        self {
                            limit(1)
                        }
                    }
                    """.trimIndent(),
                    "Unresolved reference 'limit'",
                )
                assertDoesNotCompile(
                    "SelfSelectorSortTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidSelfSelector() {
                        self {
                            sort(nearest)
                        }
                    }
                    """.trimIndent(),
                    "Unresolved reference 'sort'",
                )
            }

            "sort with all constant variants" {
                entities { sort(nearest) } shouldRenderAs "@e[sort=nearest]"
                entities { sort(furthest) } shouldRenderAs "@e[sort=furthest]"
                entities { sort(random) } shouldRenderAs "@e[sort=random]"
                entities { sort(arbitrary) } shouldRenderAs "@e[sort=arbitrary]"
            }

            "duplicate limit arguments are rejected" {
                shouldThrow<IllegalStateException> {
                    entities {
                        limit(1)
                        limit(5)
                    }
                }
            }

            "duplicate sort arguments are rejected" {
                shouldThrow<IllegalStateException> {
                    entities {
                        sort(nearest)
                        sort(furthest)
                    }
                }
            }

            "limit rejects zero" {
                shouldThrow<IllegalArgumentException> {
                    entities { limit(0) }
                }
            }

            "limit rejects negative" {
                shouldThrow<IllegalArgumentException> {
                    entities { limit(-1) }
                }
            }

            "toString returns the selector string" {
                self().toString() shouldBe "@s"
            }
        },
    )
