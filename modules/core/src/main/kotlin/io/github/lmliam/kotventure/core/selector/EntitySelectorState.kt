package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

internal class EntitySelectorState {
    var type: String? = null
    var limit: Int? = null
    var distance: SelectorRange? = null
    var sort: SelectorSort? = null
    var name: String? = null
    var level: LevelRange? = null
    var gamemode: GameMode? = null
    val tags: MutableList<String> = mutableListOf()

    fun assignType(entityType: Key) {
        type = entityType.asString()
    }

    fun assignType(entityType: String) {
        type = entityType.withDefaultNamespace()
    }
}
