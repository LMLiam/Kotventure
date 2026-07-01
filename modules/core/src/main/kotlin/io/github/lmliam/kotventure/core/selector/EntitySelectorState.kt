package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

internal class EntitySelectorState {
    var type: SelectorFilter<String>? = null
        private set
    var limit: Int? = null
    var distance: SelectorRange? = null
    var x: Double? = null
        private set
    var y: Double? = null
        private set
    var z: Double? = null
        private set
    var dx: Double? = null
        private set
    var dy: Double? = null
        private set
    var dz: Double? = null
        private set
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

    fun assignOrigin(
        x: Double?,
        y: Double?,
        z: Double?,
    ) {
        require(x != null || y != null || z != null) {
            "Selector origin requires at least one coordinate"
        }
        validateFinite("origin", "x", x)
        validateFinite("origin", "y", y)
        validateFinite("origin", "z", z)
        x?.let { this.x = it }
        y?.let { this.y = it }
        z?.let { this.z = it }
    }

    fun assignVolume(
        dx: Double?,
        dy: Double?,
        dz: Double?,
    ) {
        require(dx != null || dy != null || dz != null) {
            "Selector volume requires at least one delta"
        }
        validateFinite("volume", "dx", dx)
        validateFinite("volume", "dy", dy)
        validateFinite("volume", "dz", dz)
        dx?.let { this.dx = it }
        dy?.let { this.dy = it }
        dz?.let { this.dz = it }
    }

    private fun validateFinite(
        group: String,
        axis: String,
        value: Double?,
    ) {
        require(value == null || value.isFinite()) {
            "Selector $group $axis must be finite, got: $value"
        }
    }
}
