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
    internal val state: EntitySelectorState,
) : EntitySelectorScope {
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

    override fun distance(range: SelectorRange) {
        state.assignDistance(range)
    }

    override fun distance(range: ClosedFloatingPointRange<Double>) {
        state.assignDistance(closedRange(range.start, range.endInclusive))
    }

    override fun tag(tag: String) {
        state.addTag(tag)
    }

    override fun tag(presence: SelectorPresence) {
        state.addTag(presence)
    }

    override fun tag(tag: Excluded<String>) {
        state.excludeTag(tag.value)
    }

    override fun name(name: String) {
        state.assignName(name)
    }

    override fun name(name: Excluded<String>) {
        state.excludeName(name.value)
    }

    override fun level(range: LevelRange) {
        state.assignLevel(range)
    }

    override fun level(range: IntRange) {
        state.assignLevel(closedRange(range))
    }

    override fun gamemode(mode: GameMode) {
        state.assignGamemode(mode)
    }

    override fun gamemode(mode: Excluded<GameMode>) {
        state.excludeGamemode(mode.value)
    }

    override fun limit(n: Int) {
        require(n > 0) { "Selector limit must be positive, got: $n" }
        state.assignLimit(n)
    }

    override fun sort(sort: SelectorSort) {
        state.assignSort(sort)
    }

    override fun type(entityType: Key) {
        state.assignType(entityType)
    }

    override fun type(entityType: String) {
        state.assignType(entityType)
    }

    override fun typeTag(entityTypeTag: Key) {
        state.assignTypeTag(entityTypeTag)
    }

    override fun typeTag(entityTypeTag: Excluded<Key>) {
        state.excludeTypeTag(entityTypeTag.value)
    }
}
