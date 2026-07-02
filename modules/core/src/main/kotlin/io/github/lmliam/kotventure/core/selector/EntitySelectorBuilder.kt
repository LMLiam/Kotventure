package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.nbt.NbtCompound
import io.github.lmliam.kotventure.core.nbt.NbtCompoundBuilder
import io.github.lmliam.kotventure.core.nbt.NbtCompoundScope
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
    var level: SelectorIntRange? = null
        private set

    val typeFilters = SelectorFilterGroup<String>("type", SelectorFilterPolicy.EXCLUSIVE)
    val nameFilters = SelectorFilterGroup<String>("name", SelectorFilterPolicy.EXCLUSIVE)
    val gamemodeFilters = SelectorFilterGroup<GameMode>("gamemode", SelectorFilterPolicy.EXCLUSIVE)
    val teamFilters = SelectorFilterGroup<String>("team", SelectorFilterPolicy.EXCLUSIVE)
    val tagFilters = SelectorFilterGroup<String>("tag", SelectorFilterPolicy.REPEATABLE)
    val nbtFilters = SelectorFilterGroup<NbtCompound>("nbt", SelectorFilterPolicy.REPEATABLE)
    val predicateFilters = SelectorFilterGroup<String>("predicate", SelectorFilterPolicy.REPEATABLE)

    val coordinates: Map<SelectorAxis, Double>
        field = mutableMapOf()

    var scores: Map<String, SelectorIntRange>? = null
        private set

    var advancements: Map<String, AdvancementCondition>? = null
        private set

    private var isConfiguring = false

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

    fun configure(configure: EntitySelectorScope.() -> Unit) {
        isConfiguring = true
        try {
            configure()
        } finally {
            isConfiguring = false
        }
        validateFilters()
    }

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

    override fun tag(tag: String): SelectorFilterExpression {
        require(tag.isNotEmpty()) { "Tag name must not be empty; use tag(any) or tag(none) to filter by tag presence." }
        return tagFilters.add(this, tag)
    }

    override fun tag(presence: SelectorPresence) {
        tagFilters.addFixed(this, "", presence.polarity)
    }

    override fun nbt(init: NbtCompoundScope.() -> Unit): SelectorFilterExpression =
        nbtFilters.add(this, NbtCompoundBuilder().apply(init).build())

    override fun predicate(predicate: Key): SelectorFilterExpression = predicateFilters.add(this, predicate.asString())

    override fun name(name: String): SelectorFilterExpression = nameFilters.add(this, name)

    override fun level(range: SelectorIntRange) {
        checkUnset("level", level)
        level = range.requireNonNegative("level")
    }

    override fun level(range: IntRange) {
        level(closedRange(range))
    }

    override fun scores(init: SelectorScoresScope.() -> Unit) {
        checkUnset("scores", scores)
        scores = SelectorScoresBuilder().apply(init).scores
    }

    override fun advancements(init: SelectorAdvancementsScope.() -> Unit) {
        checkUnset("advancements", advancements)
        advancements = SelectorAdvancementsBuilder().apply(init).advancements
    }

    override fun gamemode(mode: GameMode): SelectorFilterExpression = gamemodeFilters.add(this, mode)

    override fun team(team: String): SelectorFilterExpression = teamFilters.add(this, validTeamName(team))

    override fun team(presence: SelectorPresence) {
        teamFilters.addFixed(this, "", presence.polarity)
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

    override fun type(entityType: Key): SelectorFilterExpression = typeFilters.add(this, entityType.asString())

    override fun type(entityType: String): SelectorFilterExpression =
        typeFilters.add(this, entityType.requireEntityTypeKey())

    override fun typeTag(entityTypeTag: Key): SelectorFilterExpression =
        typeFilters.add(this, entityTypeTag.asTypeTag())

    override fun SelectorFilterExpression.not() {
        check(isConfiguring) {
            "Selector filter expressions can only be negated while their selector is being configured."
        }
        (this as SelectorFilterEntry<*>).negate(this@EntitySelectorBuilder)
    }

    private fun validateFilters() {
        typeFilters.validate()
        nameFilters.validate()
        gamemodeFilters.validate()
        teamFilters.validate()
        tagFilters.validate()
        nbtFilters.validate()
        predicateFilters.validate()
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

    private fun validTeamName(team: String): String {
        require(
            team.isNotEmpty(),
        ) { "Team name must not be empty; use team(none) or team(any) to filter by team presence." }
        require(team.all { it.isAllowedInUnquotedSelectorToken() }) {
            "Team name '$team' contains characters outside vanilla's unquoted-token syntax."
        }
        return team
    }
}

private val SelectorPresence.polarity: SelectorFilterPolarity
    get() =
        when (this) {
            SelectorPresence.ANY -> SelectorFilterPolarity.NEGATIVE
            SelectorPresence.NONE -> SelectorFilterPolarity.POSITIVE
        }

private fun Key.asTypeTag(): String = "#${asString()}"
