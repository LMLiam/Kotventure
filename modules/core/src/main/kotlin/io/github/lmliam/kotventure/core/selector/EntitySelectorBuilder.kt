package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

/**
 * The single mutable builder behind every typed selector head. Head-specific safety is
 * compile-time: each factory narrows its lambda receiver to a capability scope.
 */
internal class EntitySelectorBuilder : EntitySelectorScope {
    var limit: Int? = null
        private set
    var distance: SelectorRange? = null
        private set
    var pitch: SelectorRange? = null
        private set
    var yaw: SelectorRange? = null
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

    val tags: List<String>
        field = mutableListOf()

    val coordinates: Map<SelectorAxis, Double>
        field = mutableMapOf()

    override val any: SelectorPresence get() = SelectorPresence.ANY
    override val none: SelectorPresence get() = SelectorPresence.NONE

    override val survival: GameMode get() = GameMode.SURVIVAL
    override val creative: GameMode get() = GameMode.CREATIVE
    override val adventure: GameMode get() = GameMode.ADVENTURE
    override val spectator: GameMode get() = GameMode.SPECTATOR

    override val nearest: SelectorSort get() = SelectorSort.NEAREST
    override val furthest: SelectorSort get() = SelectorSort.FURTHEST
    override val random: SelectorSort get() = SelectorSort.RANDOM
    override val arbitrary: SelectorSort get() = SelectorSort.ARBITRARY

    override fun origin(
        first: OriginCoordinate,
        vararg rest: OriginCoordinate,
    ) {
        bindCoordinates((listOf(first) + rest).map { it.axis to it.value })
    }

    override fun volume(
        first: VolumeDelta,
        vararg rest: VolumeDelta,
    ) {
        bindCoordinates((listOf(first) + rest).map { it.axis to it.value })
    }

    override fun distance(range: SelectorRange) {
        checkUnset("distance", distance)
        distance = range.requireAscending("distance").requireNonNegative("distance")
    }

    override fun distance(range: ClosedFloatingPointRange<Double>) {
        distance(closedRange(range.start, range.endInclusive))
    }

    override fun pitch(range: SelectorRange) {
        checkUnset("pitch", pitch)
        pitch = range
    }

    override fun pitch(range: ClosedFloatingPointRange<Double>) {
        pitch(closedRange(range.start, range.endInclusive))
    }

    override fun yaw(range: SelectorRange) {
        checkUnset("yaw", yaw)
        yaw = range
    }

    override fun yaw(range: ClosedFloatingPointRange<Double>) {
        yaw(closedRange(range.start, range.endInclusive))
    }

    override fun tag(tag: String) {
        tags += tag
    }

    override fun tag(presence: SelectorPresence) {
        tags += presence.value
    }

    override fun tag(tag: Excluded<String>) {
        tags += "!${tag.value}"
    }

    override fun name(name: String) {
        this.name = this.name.including("name", name)
    }

    override fun name(name: Excluded<String>) {
        this.name = this.name.excluding("name", name.value)
    }

    override fun level(range: LevelRange) {
        checkUnset("level", level)
        level = range
    }

    override fun level(range: IntRange) {
        level(closedRange(range))
    }

    override fun gamemode(mode: GameMode) {
        gamemode = gamemode.including("gamemode", mode)
    }

    override fun gamemode(mode: Excluded<GameMode>) {
        gamemode = gamemode.excluding("gamemode", mode.value)
    }

    override fun limit(n: Int) {
        require(n > 0) { "Selector limit must be positive, got: $n" }
        checkUnset("limit", limit)
        limit = n
    }

    override fun sort(sort: SelectorSort) {
        checkUnset("sort", this.sort)
        this.sort = sort
    }

    override fun type(entityType: Key) {
        type = type.including("type", entityType.asString())
    }

    override fun type(entityType: String) {
        type = type.including("type", entityType.withDefaultNamespace())
    }

    override fun typeTag(entityTypeTag: Key) {
        type = type.including("type", entityTypeTag.asTypeTag())
    }

    override fun typeTag(entityTypeTag: Excluded<Key>) {
        type = type.excluding("type", entityTypeTag.value.asTypeTag())
    }

    fun excludeType(entityType: Key) {
        type = type.excluding("type", entityType.asString())
    }

    fun excludeType(entityType: String) {
        type = type.excluding("type", entityType.withDefaultNamespace())
    }

    private fun bindCoordinates(bindings: List<Pair<SelectorAxis, Double>>) {
        val staged = mutableMapOf<SelectorAxis, Double>()
        bindings.forEach { (axis, value) ->
            checkUnset(axis.argument, coordinates[axis] ?: staged[axis])
            staged[axis] = value
        }
        coordinates += staged
    }

    private fun checkUnset(
        argument: String,
        current: Any?,
    ) {
        check(current == null) { "Selector argument '$argument' is already set; vanilla syntax allows it only once." }
    }
}

private fun Key.asTypeTag(): String = "#${asString()}"
