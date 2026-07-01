package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.test.compilation.assertDoesNotCompile
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class EntitySelectorTest :
    StringSpec(
        {
            "self returns @s" {
                self().asString() shouldBe "@s"
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

            "self accepts common and entity type arguments" {
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
                    "receiver type mismatch",
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
                val selector = entities { type(key("minecraft", "creeper")) }

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

            "negated filters accumulate in call order" {
                val selector =
                    entities {
                        type(!key("minecraft", "zombie"))
                        typeTag(!key("minecraft", "raiders"))
                        name(!"Boss")
                        name(!"Boss Mob")
                        gamemode(!survival)
                        gamemode(!creative)
                    }

                selector.asString() shouldBe
                        "@e[type=!minecraft:zombie,type=!#minecraft:raiders,name=!Boss,name=!\"Boss Mob\"," +
                        "gamemode=!survival,gamemode=!creative]"
            }

            "entity type tags use Adventure keys and preserve custom namespaces" {
                entities {
                    typeTag(key("mymod", "hostile"))
                }.asString() shouldBe "@e[type=#mymod:hostile]"

                self {
                    typeTag(!key("mymod", "ignored"))
                }.asString() shouldBe "@s[type=!#mymod:ignored]"
            }

            "negated string types apply the default namespace" {
                entities {
                    type(!"creeper")
                }.asString() shouldBe "@e[type=!minecraft:creeper]"
            }

            "tag filters support presence and mixed repeatable forms" {
                val selector =
                    allPlayers {
                        tag(any)
                        tag("vip")
                        tag(!"muted")
                        tag(none)
                    }

                selector.asString() shouldBe "@a[tag=!,tag=vip,tag=!muted,tag=]"
            }

            "origin and volume render full and partial coordinates" {
                entities {
                    origin(1.5.x, 64.y, (-2).z)
                    volume(0.dx, (-3.5).dy, 4.dz)
                }.asString() shouldBe "@e[x=1.5,y=64,z=-2,dx=0,dy=-3.5,dz=4]"

                allPlayers {
                    origin(80.y)
                    volume(0.dx, (-2).dz)
                }.asString() shouldBe "@a[y=80,dx=0,dz=-2]"
            }

            "origin and volume compose across disjoint axes" {
                val selector =
                    entities {
                        origin(1.x, 2.y)
                        origin(4.z)
                        volume(5.dx)
                        volume(6.dy, 7.dz)
                    }

                selector.asString() shouldBe "@e[x=1,y=2,z=4,dx=5,dy=6,dz=7]"
            }

            "rebinding an origin or volume axis is rejected" {
                shouldThrow<IllegalStateException> {
                    entities {
                        origin(1.x, 2.y)
                        origin(3.y)
                    }
                }

                shouldThrow<IllegalStateException> {
                    entities {
                        volume(6.dy)
                        volume((-6).dy)
                    }
                }

                shouldThrow<IllegalStateException> {
                    entities {
                        origin(1.x, 2.x)
                    }
                }
            }

            "finite coordinates render without unsupported exponent notation" {
                val selector =
                    entities {
                        origin((1e20).x, (1e-7).z)
                    }

                selector.asString() shouldBe "@e[x=100000000000000000000,z=0.0000001]"
            }

            "non-finite coordinates are rejected at construction" {
                shouldThrow<IllegalArgumentException> {
                    entities { origin(Double.POSITIVE_INFINITY.z) }
                }

                shouldThrow<IllegalArgumentException> {
                    entities { volume(Double.NaN.dy) }
                }
            }

            "failed coordinate updates do not partially mutate the selector" {
                val selector =
                    entities {
                        origin(1.x, 2.y)
                        shouldThrow<IllegalArgumentException> {
                            origin(9.x, Double.NaN.z)
                        }
                        volume(3.dx, 4.dy)
                        shouldThrow<IllegalArgumentException> {
                            volume(8.dx, Double.POSITIVE_INFINITY.dz)
                        }
                    }

                selector.asString() shouldBe "@e[x=1,y=2,dx=3,dy=4]"
            }

            "origin arguments are compile-time checked" {
                assertDoesNotCompile(
                    "InvalidOriginTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidOrigin() {
                        entities {
                            origin()
                            origin(16.dx)
                        }
                    }
                    """.trimIndent(),
                    "No value passed for parameter 'first'",
                    "Argument type mismatch",
                )
            }

            "duplicate singleton arguments are rejected" {
                shouldThrow<IllegalStateException> {
                    entities {
                        limit(1)
                        limit(5)
                    }
                }
                shouldThrow<IllegalStateException> {
                    entities {
                        sort(nearest)
                        sort(furthest)
                    }
                }
                shouldThrow<IllegalStateException> {
                    entities {
                        distance(atMost(1.0))
                        distance(atLeast(2.0))
                    }
                }
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        level(exactly(3))
                        level(5..10)
                    }
                }
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
                        type(!"skeleton")
                    }
                }
                shouldThrow<IllegalStateException> {
                    allPlayers {
                        gamemode(!survival)
                        gamemode(creative)
                    }
                }
            }

            "player selector scopes do not expose negated entity type filters" {
                assertDoesNotCompile(
                    "NegatedPlayerSelectorTypeTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    fun invalidPlayerSelector() {
                        allPlayers {
                            type(!"zombie")
                        }
                    }
                    """.trimIndent(),
                    "receiver type mismatch",
                )
            }

            "the exclusion operator is not available outside selector scopes" {
                assertDoesNotCompile(
                    "UnscopedExclusionOperatorTest.kt",
                    """
                    import io.github.lmliam.kotventure.core.selector.*

                    val excluded = !"zombie"
                    """.trimIndent(),
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
