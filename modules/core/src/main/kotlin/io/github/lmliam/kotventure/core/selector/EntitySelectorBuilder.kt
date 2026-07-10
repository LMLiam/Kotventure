package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.nbt.NbtCompound
import io.github.lmliam.kotventure.core.nbt.NbtCompoundBuilder
import io.github.lmliam.kotventure.core.nbt.NbtCompoundScope
import net.kyori.adventure.key.Key

/**
 * The single mutable builder behind every typed selector head. Head-specific safety is
 * compile-time: each factory narrows its lambda receiver to a capability scope.
 */
internal class EntitySelectorBuilder : EntitySelectorScope {
    var limit: Int? by once()
        private set
    var distance: SelectorRange? by once()
        private set
    var pitch: SelectorRange? by once()
        private set
    var yaw: SelectorRange? by once()
        private set
    var sort: SelectorSort? by once()
        private set
    var level: SelectorIntRange? by once()
        private set

    val typeFilters = SelectorFilterGroup<String>(SelectorArgumentKeyword.TYPE)
    val nameFilters = SelectorFilterGroup<String>(SelectorArgumentKeyword.NAME)
    val gamemodeFilters = SelectorFilterGroup<GameMode>(SelectorArgumentKeyword.GAMEMODE)
    val teamFilters = SelectorFilterGroup<String>(SelectorArgumentKeyword.TEAM)
    val tagFilters = SelectorFilterGroup<String>(SelectorArgumentKeyword.TAG)
    val nbtFilters = SelectorFilterGroup<NbtCompound>(SelectorArgumentKeyword.NBT)
    val predicateFilters = SelectorFilterGroup<String>(SelectorArgumentKeyword.PREDICATE)

    val coordinates: Map<SelectorCoordinate, Double>
        field = mutableMapOf()

    var scores: Map<String, SelectorIntRange>? by once()
        private set

    var advancements: Map<Key, AdvancementCondition>? by once()
        private set

    private var isConfiguring = false

    // Scope sugar: provided as overridable getters for subclass extensibility
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
        vararg rest: OriginCoordinate
    ) {
        bindCoordinates((listOf(first) + rest).map { it.coordinate to it.value })
    }

    override fun volume(
        first: VolumeDelta,
        vararg rest: VolumeDelta
    ) {
        bindCoordinates((listOf(first) + rest).map { it.coordinate to it.value })
    }

    override fun distance(range: SelectorRange) {
        distance = range.requireAscending("distance").requireNonNegative("distance")
    }

    override fun distance(range: ClosedFloatingPointRange<Double>) {
        distance(closedRange(range.start, range.endInclusive))
    }

    override fun pitch(range: SelectorRange) {
        pitch = range
    }

    override fun pitch(range: ClosedFloatingPointRange<Double>) {
        pitch(closedRange(range.start, range.endInclusive))
    }

    override fun yaw(range: SelectorRange) {
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
        level = range.requireNonNegative("level")
    }

    override fun level(range: IntRange) {
        level(closedRange(range))
    }

    override fun scores(init: SelectorScoresScope.() -> Unit) {
        scores = SelectorScoresBuilder().apply(init).scores
    }

    override fun advancements(init: SelectorAdvancementsScope.() -> Unit) {
        advancements = SelectorAdvancementsBuilder().apply(init).advancements
    }

    override fun gamemode(mode: GameMode): SelectorFilterExpression = gamemodeFilters.add(this, mode)

    override fun team(team: String): SelectorFilterExpression = teamFilters.add(this, validTeamName(team))

    override fun team(presence: SelectorPresence) {
        teamFilters.addFixed(this, "", presence.polarity)
    }

    override fun limit(n: Int) {
        require(n > 0) { "Selector limit must be positive, got: $n" }
        limit = n
    }

    override fun sort(sort: SelectorSort) {
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
        setOf(
            typeFilters,
            nameFilters,
            gamemodeFilters,
            teamFilters,
            tagFilters,
            nbtFilters,
            predicateFilters,
        ).forEach { it.validate() }
    }

    private fun bindCoordinates(bindings: List<Pair<SelectorCoordinate, Double>>) {
        val staged = mutableMapOf<SelectorCoordinate, Double>()
        bindings.forEach { (coordinate, value) ->
            check(coordinate !in coordinates && coordinate !in staged) {
                "Selector argument '${coordinate.argumentName}' may only appear once."
            }
            staged[coordinate] = value
        }
        coordinates += staged
    }

    private fun validTeamName(team: String): String {
        require(team.isNotEmpty()) {
            "Team name must not be empty; use team(none) or team(any) to filter by team presence."
        }
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
