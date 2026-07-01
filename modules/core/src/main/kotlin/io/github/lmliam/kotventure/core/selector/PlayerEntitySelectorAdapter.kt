package io.github.lmliam.kotventure.core.selector

internal open class PlayerEntitySelectorAdapter(
    state: EntitySelectorState,
) : CommonEntitySelectorAdapter(state),
    PlayerEntitySelectorScope {
    final override val nearest: SelectorSort get() = SelectorSort.NEAREST
    final override val furthest: SelectorSort get() = SelectorSort.FURTHEST
    final override val random: SelectorSort get() = SelectorSort.RANDOM
    final override val arbitrary: SelectorSort get() = SelectorSort.ARBITRARY

    final override fun limit(n: Int) {
        require(n > 0) { "Selector limit must be positive, got: $n" }
        state.limit = n
    }

    final override fun sort(sort: SelectorSort) {
        state.sort = sort
    }
}
