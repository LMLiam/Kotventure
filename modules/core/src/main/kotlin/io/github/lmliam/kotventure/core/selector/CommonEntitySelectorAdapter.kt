package io.github.lmliam.kotventure.core.selector

internal abstract class CommonEntitySelectorAdapter(
    protected val state: EntitySelectorState,
) : CommonEntitySelectorScope {
    final override val survival: GameMode get() = GameMode.SURVIVAL
    final override val creative: GameMode get() = GameMode.CREATIVE
    final override val adventure: GameMode get() = GameMode.ADVENTURE
    final override val spectator: GameMode get() = GameMode.SPECTATOR

    final override fun distance(range: SelectorRange) {
        state.distance = range
    }

    final override fun distance(range: ClosedFloatingPointRange<Double>) {
        require(!range.isEmpty()) { "Range must not be empty, got: $range" }
        state.distance = closedRange(range.start, range.endInclusive)
    }

    final override fun tag(tag: String) {
        state.tags += tag
    }

    final override fun name(name: String) {
        state.name = name
    }

    final override fun level(range: LevelRange) {
        state.level = range
    }

    final override fun level(range: IntRange) {
        require(!range.isEmpty()) { "Range must not be empty, got: $range" }
        state.level = LevelRange("${range.first}..${range.last}")
    }

    final override fun gamemode(mode: GameMode) {
        state.gamemode = mode
    }
}
