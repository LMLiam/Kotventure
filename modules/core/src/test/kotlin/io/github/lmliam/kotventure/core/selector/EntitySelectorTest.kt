package io.github.lmliam.kotventure.core.selector

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

            "distance with between" {
                val selector = entities { distance(between(1.5, 10.5)) }

                selector.asString() shouldBe "@e[distance=1.5..10.5]"
            }

            "level with range" {
                val selector = allPlayers { level(between(5.0, 30.0)) }

                selector.asString() shouldBe "@a[level=5..30]"
            }

            "type with Adventure Key" {
                val selector = entities { type(Key.key("minecraft", "creeper")) }

                selector.asString() shouldBe "@e[type=minecraft:creeper]"
            }

            "gamemode filter" {
                val selector = allPlayers { gamemode("survival") }

                selector.asString() shouldBe "@a[gamemode=survival]"
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

            "between rejects NaN in min" {
                shouldThrow<IllegalArgumentException> {
                    between(Double.NaN, 10.0)
                }
            }

            "between rejects NaN in max" {
                shouldThrow<IllegalArgumentException> {
                    between(1.0, Double.NaN)
                }
            }

            "between rejects infinity in min" {
                shouldThrow<IllegalArgumentException> {
                    between(Double.POSITIVE_INFINITY, 10.0)
                }
            }

            "between rejects infinity in max" {
                shouldThrow<IllegalArgumentException> {
                    between(1.0, Double.POSITIVE_INFINITY)
                }
            }

            "between rejects inverted range" {
                shouldThrow<IllegalArgumentException> {
                    between(10.0, 5.0)
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
