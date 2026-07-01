package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.test.compilation.assertCompiles
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import net.kyori.adventure.key.Key

class EntitySelectorTest :
    StringSpec(
        {
            "self returns @s" {
                self().asString() shouldBe "@s"
            }

            "self preserves its no-argument JVM entry point" {
                val factoryClass =
                    Class.forName(
                        "io.github.lmliam.kotventure.core.selector.EntitySelectorFactoryKt",
                    )

                factoryClass.getMethod("self").invoke(null).toString() shouldBe "@s"
            }

            "nearestPlayer with no arguments returns @p" {
                nearestPlayer().asString() shouldBe "@p"
            }

            "allPlayers with no arguments returns @a" {
                allPlayers().asString() shouldBe "@a"
            }

            "randomPlayer with no arguments returns @r" {
                randomPlayer().asString() shouldBe "@r"
            }

            "entities with no arguments returns @e" {
                entities().asString() shouldBe "@e"
            }

            "nearestEntity with no arguments returns @n" {
                nearestEntity().asString() shouldBe "@n"
            }

            "all six selector heads expose their typed factories" {
                assertCompiles(
                    "AllSelectorHeadsTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun allHeads() {
                        nearestPlayer()
                        allPlayers()
                        randomPlayer()
                        self()
                        entities()
                        nearestEntity()
                    }
                    """.trimIndent(),
                )
            }

            "self accepts common and entity type arguments" {
                assertCompiles(
                    "ConfiguredSelfSelectorTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun configuredSelf() {
                        self {
                            type("minecraft:zombie")
                            name("Boss")
                        }
                    }
                    """.trimIndent(),
                )

                self {
                    type("minecraft:zombie")
                    name("Boss")
                }.asString() shouldBe "@s[type=minecraft:zombie,name=Boss]"
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

            "player selector scopes retain limit and sort" {
                val selector =
                    nearestPlayer {
                        limit(3)
                        sort(nearest)
                    }

                selector.asString() shouldBe "@p[limit=3,sort=nearest]"
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

            "entities with type and limit" {
                val selector =
                    entities {
                        type("armor_stand")
                        limit(1)
                    }

                selector.asString() shouldBe "@e[type=minecraft:armor_stand,limit=1]"
            }

            "nearestPlayer with typed distance range" {
                val selector = nearestPlayer { distance(atMost(10.0)) }

                selector.asString() shouldBe "@p[distance=..10]"
            }

            "allPlayers with tag" {
                val selector = allPlayers { tag("admin") }

                selector.asString() shouldBe "@a[tag=admin]"
            }

            "entities with name and scoped sort constant" {
                val selector =
                    entities {
                        name("Dinnerbone")
                        sort(nearest)
                    }

                selector.asString() shouldBe "@e[name=Dinnerbone,sort=nearest]"
            }

            "distance with Kotlin range" {
                val selector = nearestPlayer { distance(5.0..20.0) }

                selector.asString() shouldBe "@p[distance=5..20]"
            }

            "distance with atLeast" {
                val selector = entities { distance(atLeast(3.0)) }

                selector.asString() shouldBe "@e[distance=3..]"
            }

            "distance with exactly" {
                val selector = entities { distance(exactly(5.0)) }

                selector.asString() shouldBe "@e[distance=5]"
            }

            "distance with inverted Kotlin range is rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { distance(10.0..1.0) }
                }
            }

            "distance with non-finite Kotlin range is rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { distance(1.0..Double.POSITIVE_INFINITY) }
                }
            }

            "level with Kotlin IntRange" {
                val selector = allPlayers { level(5..30) }

                selector.asString() shouldBe "@a[level=5..30]"
            }

            "level with open-ended range" {
                val selector = allPlayers { level(atLeast(10)) }

                selector.asString() shouldBe "@a[level=10..]"
            }

            "level with atMost bound" {
                val selector = allPlayers { level(atMost(30)) }

                selector.asString() shouldBe "@a[level=..30]"
            }

            "level with exact value" {
                val selector = allPlayers { level(exactly(5)) }

                selector.asString() shouldBe "@a[level=5]"
            }

            "level with inverted IntRange is rejected" {
                shouldThrow<IllegalArgumentException> {
                    entities { level(5..1) }
                }
            }

            "type with Adventure Key" {
                val selector = entities { type(Key.key("minecraft", "creeper")) }

                selector.asString() shouldBe "@e[type=minecraft:creeper]"
            }

            "gamemode filter with typed constant" {
                val selector = allPlayers { gamemode(survival) }

                selector.asString() shouldBe "@a[gamemode=survival]"
            }

            "gamemode with all constant variants" {
                allPlayers { gamemode(survival) }.asString() shouldBe "@a[gamemode=survival]"
                allPlayers { gamemode(creative) }.asString() shouldBe "@a[gamemode=creative]"
                allPlayers { gamemode(adventure) }.asString() shouldBe "@a[gamemode=adventure]"
                allPlayers { gamemode(spectator) }.asString() shouldBe "@a[gamemode=spectator]"
            }

            "sort with all constant variants" {
                entities { sort(nearest) }.asString() shouldBe "@e[sort=nearest]"
                entities { sort(furthest) }.asString() shouldBe "@e[sort=furthest]"
                entities { sort(random) }.asString() shouldBe "@e[sort=random]"
                entities { sort(arbitrary) }.asString() shouldBe "@e[sort=arbitrary]"
            }

            "escape hatch wraps a raw string" {
                val selector = entitySelector("@e[type=zombie,limit=5,nbt={NoAI:1b}]")

                selector.asString() shouldBe "@e[type=zombie,limit=5,nbt={NoAI:1b}]"
            }

            "toString returns the selector string" {
                self().toString() shouldBe "@s"
            }

            "type with already prefixed namespace does not double-prefix" {
                val selector = entities { type("minecraft:zombie") }

                selector.asString() shouldBe "@e[type=minecraft:zombie]"
            }

            "type with custom namespace is preserved" {
                val selector = entities { type("mymod:custom_entity") }

                selector.asString() shouldBe "@e[type=mymod:custom_entity]"
            }

            "name with special characters is quoted" {
                val selector = nearestPlayer { name("Player, [Admin]") }

                selector.asString() shouldBe "@p[name=\"Player, [Admin]\"]"
            }

            "name with quotes is escaped" {
                val selector = nearestPlayer { name("Bob's \"Special\" Name") }

                selector.asString() shouldBe "@p[name=\"Bob's \\\"Special\\\" Name\"]"
            }

            "name without special characters is not quoted" {
                val selector = nearestPlayer { name("SimplePlayer") }

                selector.asString() shouldBe "@p[name=SimplePlayer]"
            }

            "name outside Brigadier's unquoted character set is quoted" {
                val selector = nearestPlayer { name("namespace:value") }

                selector.asString() shouldBe "@p[name=\"namespace:value\"]"
            }

            "atMost rejects NaN" {
                shouldThrow<IllegalArgumentException> {
                    atMost(Double.NaN)
                }
            }

            "atMost rejects positive infinity" {
                shouldThrow<IllegalArgumentException> {
                    atMost(Double.POSITIVE_INFINITY)
                }
            }

            "atMost rejects negative infinity" {
                shouldThrow<IllegalArgumentException> {
                    atMost(Double.NEGATIVE_INFINITY)
                }
            }

            "atLeast rejects NaN" {
                shouldThrow<IllegalArgumentException> {
                    atLeast(Double.NaN)
                }
            }

            "atLeast rejects infinity" {
                shouldThrow<IllegalArgumentException> {
                    atLeast(Double.POSITIVE_INFINITY)
                }
            }

            "atLeast rejects negative infinity" {
                shouldThrow<IllegalArgumentException> {
                    atLeast(Double.NEGATIVE_INFINITY)
                }
            }

            "exactly rejects NaN" {
                shouldThrow<IllegalArgumentException> {
                    exactly(Double.NaN)
                }
            }

            "exactly rejects infinity" {
                shouldThrow<IllegalArgumentException> {
                    exactly(Double.POSITIVE_INFINITY)
                }
            }

            "exactly rejects negative infinity" {
                shouldThrow<IllegalArgumentException> {
                    exactly(Double.NEGATIVE_INFINITY)
                }
            }

            "singleton options use last-write-wins" {
                val selector =
                    entities {
                        limit(1)
                        limit(5)
                        sort(nearest)
                        sort(furthest)
                    }

                selector.asString() shouldBe "@e[limit=5,sort=furthest]"
            }

            "multiple tags are all preserved" {
                val selector =
                    entities {
                        tag("admin")
                        tag("vip")
                    }

                selector.asString() shouldBe "@e[tag=admin,tag=vip]"
            }

            "name with backslash and special chars is escaped" {
                val selector = nearestPlayer { name("path\\to [file]") }

                selector.asString() shouldBe "@p[name=\"path\\\\to [file]\"]"
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
        },
    )
