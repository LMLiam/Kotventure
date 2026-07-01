package io.github.lmliam.kotventure.core.selector

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
        state.distance = range
    }

    final override fun distance(range: ClosedFloatingPointRange<Double>) {
        require(!range.isEmpty()) { "Range must not be empty, got: $range" }
        state.distance = closedRange(range.start, range.endInclusive)
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

    final override fun name(name: String) {
        state.assignName(name)
    }

    final override fun level(range: LevelRange) {
        state.level = range
    }

    final override fun level(range: IntRange) {
        require(!range.isEmpty()) { "Range must not be empty, got: $range" }
        state.level = LevelRange("${range.first}..${range.last}")
    }

    final override fun gamemode(mode: GameMode) {
        state.assignGamemode(mode)
    }
}
