package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

/**
 * Shared mutable selector state, enforcing vanilla's argument rules in one place: every singleton
 * argument binds once, a positive value and exclusions never coexist, exclusions and tags
 * accumulate in call order.
 */
internal class EntitySelectorState {
    var limit: Int? = null
        private set
    var distance: SelectorRange? = null
        private set
    var sort: SelectorSort? = null
        private set
    var level: LevelRange? = null
        private set

    var type: SelectorFilter<String>? = null
        private set
    var name: SelectorFilter<String>? = null
        private set
    var gamemode: SelectorFilter<GameMode>? = null
        private set

    private val mutableTags = mutableListOf<String>()
    val tags: List<String> get() = mutableTags

    fun assignLimit(value: Int) {
        checkUnset("limit", limit)
        limit = value
    }

    fun assignDistance(value: SelectorRange) {
        checkUnset("distance", distance)
        distance = value
    }

    fun assignSort(value: SelectorSort) {
        checkUnset("sort", sort)
        sort = value
    }

    fun assignLevel(value: LevelRange) {
        checkUnset("level", level)
        level = value
    }

    fun assignType(entityType: Key) {
        type = type.including("type", entityType.asString())
    }

    fun assignType(entityType: String) {
        type = type.including("type", entityType.withDefaultNamespace())
    }

    fun assignTypeTag(entityTypeTag: Key) {
        type = type.including("type", entityTypeTag.asTypeTag())
    }

    fun excludeType(entityType: Key) {
        type = type.excluding("type", entityType.asString())
    }

    fun excludeType(entityType: String) {
        type = type.excluding("type", entityType.withDefaultNamespace())
    }

    fun excludeTypeTag(entityTypeTag: Key) {
        type = type.excluding("type", entityTypeTag.asTypeTag())
    }

    fun assignName(value: String) {
        name = name.including("name", value)
    }

    fun excludeName(value: String) {
        name = name.excluding("name", value)
    }

    fun assignGamemode(value: GameMode) {
        gamemode = gamemode.including("gamemode", value)
    }

    fun excludeGamemode(value: GameMode) {
        gamemode = gamemode.excluding("gamemode", value)
    }

    fun addTag(value: String) {
        mutableTags += value
    }

    fun addTag(presence: SelectorPresence) {
        mutableTags += presence.value
    }

    fun excludeTag(value: String) {
        mutableTags += "!$value"
    }

    private fun checkUnset(
        argument: String,
        current: Any?,
    ) {
        check(current == null) { "Selector argument '$argument' is already set; vanilla syntax allows it only once." }
    }
}

private fun Key.asTypeTag(): String = "#${asString()}"
