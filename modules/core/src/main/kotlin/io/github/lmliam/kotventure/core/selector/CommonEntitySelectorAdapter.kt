package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.nbt.NbtCompoundScope
import net.kyori.adventure.key.Key

internal abstract class CommonEntitySelectorAdapter(
    final override val selectorState: EntitySelectorState,
) : CommonEntitySelectorScope,
    EntitySelectorStateOwner {
    protected val state: EntitySelectorState get() = selectorState

    final override val any: SelectorPresence get() = SelectorPresence.ANY
    final override val none: SelectorPresence get() = SelectorPresence.NONE

    final override val survival: GameMode get() = GameMode.SURVIVAL
    final override val creative: GameMode get() = GameMode.CREATIVE
    final override val adventure: GameMode get() = GameMode.ADVENTURE
    final override val spectator: GameMode get() = GameMode.SPECTATOR

    final override fun distance(range: SelectorRange) {
        state.distance = validateDistanceRange(range)
    }

    final override fun distance(range: ClosedFloatingPointRange<Double>) {
        state.distance = validateDistanceRange(closedRange(range.start, range.endInclusive))
    }

    final override fun xRotation(range: SelectorRange) {
        state.xRotation = range
    }

    final override fun xRotation(range: ClosedFloatingPointRange<Double>) {
        state.xRotation = closedRange(range.start, range.endInclusive)
    }

    final override fun yRotation(range: SelectorRange) {
        state.yRotation = range
    }

    final override fun yRotation(range: ClosedFloatingPointRange<Double>) {
        state.yRotation = closedRange(range.start, range.endInclusive)
    }

    final override fun origin(
        x: Double?,
        y: Double?,
        z: Double?,
    ) {
        state.assignOrigin(x, y, z)
    }

    final override fun volume(
        dx: Double?,
        dy: Double?,
        dz: Double?,
    ) {
        state.assignVolume(dx, dy, dz)
    }

    final override fun tag(tag: String) {
        state.addTag(tag)
    }

    final override fun tag(presence: SelectorPresence) {
        state.addTag(presence)
    }

    final override fun team(team: String) {
        state.assignTeam(team)
    }

    final override fun team(presence: SelectorPresence) {
        state.assignTeam(presence)
    }

    final override fun nbt(init: NbtCompoundScope.() -> Unit) {
        state.addNbtFilter(isNegated = false, init)
    }

    final override fun score(
        objective: String,
        range: LevelRange,
    ) {
        state.assignScore(objective, range)
    }

    final override fun score(
        objective: String,
        range: IntRange,
    ) {
        state.assignScore(objective, closedLevelRange(range.first, range.last))
    }

    final override fun predicate(predicate: Key) {
        state.addPredicateFilter(predicate, isNegated = false)
    }

    final override fun name(name: String) {
        state.assignName(name)
    }

    final override fun level(range: LevelRange) {
        state.level = validateLevelRange(range)
    }

    final override fun level(range: IntRange) {
        state.level = validateLevelRange(closedLevelRange(range.first, range.last))
    }

    final override fun gamemode(mode: GameMode) {
        state.assignGamemode(mode)
    }

    private fun validateDistanceRange(range: SelectorRange): SelectorRange {
        val minimum = range.minimum
        val maximum = range.maximum
        require(
            (minimum == null || minimum >= 0.0) &&
                (maximum == null || maximum >= 0.0),
        ) {
            "Distance range bounds must be non-negative, got: $range"
        }
        require(
            minimum == null ||
                maximum == null ||
                minimum <= maximum,
        ) {
            "Distance range min ($minimum) must not exceed max ($maximum)"
        }
        return range
    }

    private fun validateLevelRange(range: LevelRange): LevelRange {
        val minimum = range.minimum
        val maximum = range.maximum
        require(
            (minimum == null || minimum >= 0) &&
                (maximum == null || maximum >= 0),
        ) {
            "Level range bounds must be non-negative, got: $range"
        }
        return range
    }
}
