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

internal fun negatedCommonEntitySelectorScopeSample() {
    allPlayers {
        not {
            name("Bot")
            tag("hidden")
        }
    }
}

internal fun negatedPlayerEntitySelectorScopeSample() {
    allPlayers {
        not { gamemode(spectator) }
    }
}

internal fun negatedSelfEntitySelectorScopeSample() {
    self {
        not { type(Key.key("minecraft", "player")) }
    }
}

internal fun negatedEntitySelectorScopeSample() {
    entities {
        not { typeTag(Key.key("minecraft", "undead")) }
    }
}
