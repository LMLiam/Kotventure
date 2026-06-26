package io.github.lmliam.kotventure.core.selector

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
        },
    )
