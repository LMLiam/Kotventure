package io.github.lmliam.kotventure.core.selector

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
