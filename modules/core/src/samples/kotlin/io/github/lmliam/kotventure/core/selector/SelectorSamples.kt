package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

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
        origin(x = 12.5, y = 64.0)
        volume(dx = 0.0, dy = 4.0, dz = -2.0)
    }
}

internal fun selectorRotationSample() {
    entities {
        xRotation(atMost(45.0))
        yRotation(170.0..-170.0)
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

internal fun playerSelectorNegationSample() {
    allPlayers {
        not {
            name("Bot")
            gamemode(spectator)
        }
    }
}

internal fun selfSelectorNegationSample() {
    self {
        not { type(Key.key("minecraft", "player")) }
    }
}

internal fun entitySelectorNegationSample() {
    entities {
        typeTag(Key.key("minecraft", "raiders"))
        not {
            type(Key.key("minecraft", "pillager"))
            tag("hidden")
        }
    }
}

internal fun selectorPresenceSample() {
    allPlayers {
        tag(any)
        tag(none)
    }
}

internal fun negatedCommonEntitySelectorScopeSample() {
    allPlayers {
        not {
            name("Bot")
            tag("hidden")
        }
    }
}

internal fun negatedEntitySelectorScopeSample() {
    entities {
        not { typeTag(Key.key("minecraft", "undead")) }
    }
}
