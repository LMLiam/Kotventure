package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

/**
 * The single mutable adapter behind every typed selector head.
 *
 * It implements the complete [EntitySelectorScope]; each factory restricts what a caller can reach by
 * typing its lambda receiver to the narrower capability scope. That keeps head-specific safety a
 * compile-time property of the interfaces while the runtime plumbing stays in one place.
 */
internal class EntitySelectorAdapter(
    private val state: EntitySelectorState,
) : EntitySelectorScope {
    override val survival: GameMode get() = GameMode.SURVIVAL
    override val creative: GameMode get() = GameMode.CREATIVE
    override val adventure: GameMode get() = GameMode.ADVENTURE
    override val spectator: GameMode get() = GameMode.SPECTATOR

    override val nearest: SelectorSort get() = SelectorSort.NEAREST
    override val furthest: SelectorSort get() = SelectorSort.FURTHEST
    override val random: SelectorSort get() = SelectorSort.RANDOM
    override val arbitrary: SelectorSort get() = SelectorSort.ARBITRARY

    override fun distance(range: SelectorRange) {
        state.distance = range
    }

    override fun distance(range: ClosedFloatingPointRange<Double>) {
        require(!range.isEmpty()) { "Range must not be empty, got: $range" }
        state.distance = closedRange(range.start, range.endInclusive)
    }

    override fun tag(tag: String) {
        state.tags += tag
    }

    override fun name(name: String) {
        state.name = name
    }

    override fun level(range: LevelRange) {
        state.level = range
    }

    override fun level(range: IntRange) {
        require(!range.isEmpty()) { "Range must not be empty, got: $range" }
        state.level = LevelRange("${range.first}..${range.last}")
    }

    override fun gamemode(mode: GameMode) {
        state.gamemode = mode
    }

    override fun limit(n: Int) {
        require(n > 0) { "Selector limit must be positive, got: $n" }
        state.limit = n
    }

    override fun sort(sort: SelectorSort) {
        state.sort = sort
    }

    override fun type(entityType: Key) {
        state.assignType(entityType)
    }

    override fun type(entityType: String) {
        state.assignType(entityType)
    }
}
