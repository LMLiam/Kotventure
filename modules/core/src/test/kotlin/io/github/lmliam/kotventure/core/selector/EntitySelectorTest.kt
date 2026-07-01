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

            "distance ranges render without unsupported exponent notation" {
                entities {
                    distance(exactly(1e20))
                }.asString() shouldBe "@e[distance=100000000000000000000]"

                entities {
                    distance(atMost(1e-7))
                }.asString() shouldBe "@e[distance=..0.0000001]"
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

            "negated singleton filters accumulate in call order" {
                val selector =
                    entities {
                        not {
                            type(Key.key("minecraft", "zombie"))
                            typeTag(Key.key("minecraft", "raiders"))
                            name("Boss")
                            name("Boss Mob")
                            gamemode(survival)
                            gamemode(creative)
                        }
                    }

                selector.asString() shouldBe
                    "@e[type=!minecraft:zombie,type=!#minecraft:raiders,name=!Boss,name=!\"Boss Mob\"," +
                    "gamemode=!survival,gamemode=!creative]"
            }

            "latest singleton polarity replaces the previous polarity" {
                entities {
                    type(Key.key("minecraft", "zombie"))
                    not {
                        type(Key.key("minecraft", "skeleton"))
                        type(Key.key("minecraft", "creeper"))
                    }
                }.asString() shouldBe "@e[type=!minecraft:skeleton,type=!minecraft:creeper]"

                entities {
                    not {
                        name("Boss")
                        name("Minion")
                    }
                    name("Alex")
                }.asString() shouldBe "@e[name=Alex]"

                allPlayers {
                    not {
                        gamemode(survival)
                        gamemode(creative)
                    }
                    gamemode(adventure)
                }.asString() shouldBe "@a[gamemode=adventure]"
            }

            "entity type tags use Adventure keys and preserve custom namespaces" {
                entities {
                    typeTag(Key.key("mymod", "hostile"))
                }.asString() shouldBe "@e[type=#mymod:hostile]"

                self {
                    not {
                        typeTag(Key.key("mymod", "ignored"))
                    }
                }.asString() shouldBe "@s[type=!#mymod:ignored]"
            }

            "tag filters support presence and mixed repeatable forms" {
                val selector =
                    allPlayers {
                        tag(any)
                        tag("vip")
                        not { tag("muted") }
                        tag(none)
                    }

                selector.asString() shouldBe "@a[tag=!,tag=vip,tag=!muted,tag=]"
            }

            "origin and volume render full and partial coordinates" {
                entities {
                    origin(x = 1.5, y = 64.0, z = -2.0)
                    volume(dx = 0.0, dy = -3.5, dz = 4.0)
                }.asString() shouldBe "@e[x=1.5,y=64,z=-2,dx=0,dy=-3.5,dz=4]"

                allPlayers {
                    origin(y = 80.0)
                    volume(dx = 0.0, dz = -2.0)
                }.asString() shouldBe "@a[y=80,dx=0,dz=-2]"
            }

            "repeated origin and volume calls replace only supplied axes" {
                val selector =
                    entities {
                        origin(x = 1.0, y = 2.0)
                        origin(y = 3.0, z = 4.0)
                        volume(dx = 5.0, dy = 6.0, dz = 7.0)
                        volume(dy = -6.0)
                    }

                selector.asString() shouldBe "@e[x=1,y=3,z=4,dx=5,dy=-6,dz=7]"
            }

            "finite coordinates render without unsupported exponent notation" {
                val selector =
                    entities {
                        origin(x = 1e20, z = 1e-7)
                    }

                selector.asString() shouldBe "@e[x=100000000000000000000,z=0.0000001]"
            }

            "origin rejects empty and non-finite coordinates" {
                shouldThrow<IllegalArgumentException> {
                    entities { origin() }
                }.message shouldBe "Selector origin requires at least one coordinate"

                shouldThrow<IllegalArgumentException> {
                    entities { origin(z = Double.POSITIVE_INFINITY) }
                }.message shouldBe "Selector origin z must be finite, got: Infinity"
            }

            "failed coordinate updates do not partially mutate the selector" {
                val selector =
                    entities {
                        origin(x = 1.0, y = 2.0)
                        shouldThrow<IllegalArgumentException> {
                            origin(x = 9.0, z = Double.NaN)
                        }
                        volume(dx = 3.0, dy = 4.0)
                        shouldThrow<IllegalArgumentException> {
                            volume(dx = 8.0, dz = Double.POSITIVE_INFINITY)
                        }
                    }

                selector.asString() shouldBe "@e[x=1,y=2,dx=3,dy=4]"
            }

            "volume rejects empty and non-finite deltas" {
                shouldThrow<IllegalArgumentException> {
                    entities { volume() }
                }.message shouldBe "Selector volume requires at least one delta"

                shouldThrow<IllegalArgumentException> {
                    entities { volume(dy = Double.NaN) }
                }.message shouldBe "Selector volume dy must be finite, got: NaN"
            }

            "player negation scopes do not expose entity type filters" {
                assertDoesNotCompile(
                    "NegatedPlayerSelectorTypeTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidPlayerSelector() {
                        allPlayers {
                            not {
                                type("minecraft:zombie")
                            }
                        }
                    }
                    """.trimIndent(),
                    "Unresolved reference 'type'",
                )
            }

            "negated scopes cannot call positive filters through an outer receiver" {
                assertDoesNotCompile(
                    "NegatedSelectorOuterScopeTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidNegatedSelector() {
                        allPlayers {
                            not {
                                distance(atMost(1.0))
                            }
                        }
                    }
                    """.trimIndent(),
                    "implicit receiver",
                )
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
