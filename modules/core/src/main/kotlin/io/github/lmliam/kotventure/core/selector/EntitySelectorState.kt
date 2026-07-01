package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

internal class EntitySelectorState {
    var type: SelectorFilter<String>? = null
        private set
    var limit: Int? = null
    var distance: SelectorRange? = null
    var sort: SelectorSort? = null
    var name: SelectorFilter<String>? = null
        private set
    var level: LevelRange? = null
    var gamemode: SelectorFilter<GameMode>? = null
        private set
    val tags: MutableList<String> = mutableListOf()

    fun assignType(entityType: Key) {
        assignType(entityType.asString())
    }

    fun assignType(entityType: String) {
        type = entityType.withDefaultNamespace().asIncludedFilter()
    }

    fun assignTypeTag(entityTypeTag: Key) {
        type = "#${entityTypeTag.asString()}".asIncludedFilter()
    }

    fun excludeType(entityType: String) {
        type = type.excluding(entityType)
    }

    fun excludeTypeTag(entityTypeTag: Key) {
        type = type.excluding("#${entityTypeTag.asString()}")
    }

    fun assignName(value: String) {
        name = value.asIncludedFilter()
    }

    fun excludeName(value: String) {
        name = name.excluding(value)
    }

    fun assignGamemode(value: GameMode) {
        gamemode = value.asIncludedFilter()
    }

    fun excludeGamemode(value: GameMode) {
        gamemode = gamemode.excluding(value)
    }

    fun addTag(value: String) {
        tags += value
    }

    fun addTag(presence: SelectorPresence) {
        tags += presence.value
    }

    fun excludeTag(value: String) {
        tags += "!$value"
    }
}
