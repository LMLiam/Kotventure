package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.nbt.list
import io.kotest.core.spec.style.StringSpec

class VanillaSelectorConformanceTest :
    StringSpec({
    "accepts all canonical selector heads" {
        listOf(
            self(),
            nearestPlayer(),
            allPlayers(),
            randomPlayer(),
            entities(),
            nearestEntity(),
        ).forEach { it.shouldBeAcceptedByVanilla() }
    }

    "accepts every canonical argument emitted by the typed DSL" {
        val canonical =
            entities {
            typeTag(key("minecraft", "raiders"))
            name("Boss Mob")
            origin(12.5.x, 64.y, (-4.0).z)
            volume(16.dx, 8.dy, (-16.0).dz)
            distance(0.0..64.0)
            pitch(atMost(45.0))
            yaw(170.0..-170.0)
            level(0..30)
            gamemode(survival)
            limit(5)
            sort(nearest)
            tag("boss")
            team("raiders")
            nbt { "Tags" eq list("boss", "hostile") }
            scores { "kills" eq atLeast(10) }
            predicate(key("minecraft", "is_baby"))
            advancements { key("minecraft", "story/root") eq true }
        }

        canonical.shouldBeAcceptedByVanilla()
    }

    "accepts capability-specific output for every selector head" {
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
            self { type(key("minecraft", "zombie")) },
            entities {
                type(key("minecraft", "zombie"))
                sort(furthest)
                limit(3)
            },
            nearestEntity {
                typeTag(key("minecraft", "undead"))
                limit(1)
            },
        ).forEach { it.shouldBeAcceptedByVanilla() }
    }

    "accepts repeated, empty, quoted, negated and boundary forms" {
        val repeated =
            entities {
            tag(any)
            tag(none)
            tag("visible")
            !type(key("minecraft", "zombie"))
            !type(key("minecraft", "skeleton"))
            !typeTag(key("minecraft", "undead"))
            !name("Bot")
            !gamemode(creative)
            !tag("hidden")
            !team("red")
            !team("blue")
            !nbt { "Invisible" eq true }
            !predicate(key("my_pack", "hidden"))
            team(any)
            nbt { "Health" eq 20.0f }
            predicate(key("my_pack", "visible"))
            advancements { key("my_pack", "secret") eq { "found_item" eq false } }
        }

        repeated.shouldBeAcceptedByVanilla()
        entities { name("Boss \"Mob\"") }.shouldBeAcceptedByVanilla()

        val boundaries =
            entities {
            origin(Double.MIN_VALUE.x, Double.MAX_VALUE.y, (-Double.MAX_VALUE).z)
            distance(atLeast(0.0))
            pitch(-180.0..180.0)
            yaw(170.0..-170.0)
            level(0..Int.MAX_VALUE)
            limit(Int.MAX_VALUE)
        }

        boundaries.shouldBeAcceptedByVanilla()
    }

    "rejects intentionally invalid vanilla selectors" {
        listOf(
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
        ).forEach { it.shouldBeRejectedByVanilla() }
    }
})
