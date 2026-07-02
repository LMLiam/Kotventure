package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.key.key
import io.github.lmliam.kotventure.core.nbt.list

internal fun selectorSample() {
    val nearby = selector(entities { distance(atMost(10.0)) }) { separator { content(", ") } }
}

internal fun nearestPlayerSample() {
    nearestPlayer { distance(atMost(10.0)) }
}

internal fun playerEntitySelectorScopeSample() {
    allPlayers {
        distance(atMost(10.0))
        sort(nearest)
        limit(5)
    }
}

internal fun allPlayersSample() {
    allPlayers { tag("admin") }
}

internal fun randomPlayerSample() {
    randomPlayer { distance(atMost(10.0)) }
}

internal fun selfSample() {
    self {
        type("minecraft:player")
        tag("active")
    }
}

internal fun selfEntitySelectorScopeSample() {
    self {
        type("minecraft:player")
        name("Alex")
    }
}

internal fun commonEntitySelectorScopeSample() {
    self {
        distance(atMost(10.0))
        tag("active")
    }
}

internal fun selectorPositionVolumeSample() {
    entities {
        origin(12.5.x, 64.y)
        volume(16.dx, 8.dy, 16.dz)
    }
}

internal fun selectorRotationSample() {
    entities {
        pitch(-90.0..-45.0)
        yaw(170.0..-170.0)
    }
}

internal fun selectorScoreSample() {
    allPlayers {
        scores {
            "kills" eq atLeast(10)
            "deaths" eq 0..5
        }
    }
}

internal fun selectorTeamSample() {
    allPlayers { team("red") }
    entities { team(none) }
    entities {
        team(any)
        !team("spectators")
    }
}

internal fun entitiesSample() {
    entities {
        type("armor_stand")
        distance(atMost(10.0))
        sort(nearest)
        limit(1)
        tag("display")
    }
}

internal fun nearestEntitySample() {
    nearestEntity {
        type("minecraft:zombie")
        distance(atMost(10.0))
    }
}

internal fun entitySelectorScopeSample() {
    entities {
        type("armor_stand")
        distance(atMost(10.0))
        sort(nearest)
        limit(1)
        tag("display")
    }
}

internal fun selectorPresenceSample() {
    allPlayers {
        tag(any)
        tag(none)
    }
}

internal fun selectorNbtSample() {
    entities {
        nbt {
            "Health" eq 20.0f
            "Tags" eq list("boss", "hostile")
        }
        !nbt { "Invisible" eq true }
    }
}

internal fun negatedCommonArgumentsSample() {
    allPlayers {
        !name("Bot")
        !gamemode(spectator)
        tag("vip")
        !tag("muted")
    }
}

internal fun negatedTypeArgumentsSample() {
    entities {
        !type("zombie")
        !type(key("minecraft", "skeleton"))
        !typeTag(key("minecraft", "raiders"))
    }
}
