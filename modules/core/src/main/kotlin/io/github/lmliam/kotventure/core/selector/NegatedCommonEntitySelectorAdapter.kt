package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.nbt.NbtCompoundScope
import net.kyori.adventure.key.Key

internal open class NegatedCommonEntitySelectorAdapter(
    protected val state: EntitySelectorState,
) : NegatedCommonEntitySelectorScope {
    final override val survival: GameMode get() = GameMode.SURVIVAL
    final override val creative: GameMode get() = GameMode.CREATIVE
    final override val adventure: GameMode get() = GameMode.ADVENTURE
    final override val spectator: GameMode get() = GameMode.SPECTATOR

    final override fun name(name: String) {
        state.excludeName(name)
    }

    final override fun tag(tag: String) {
        state.excludeTag(tag)
    }

    final override fun team(team: String) {
        state.excludeTeam(team)
    }

    final override fun nbt(init: NbtCompoundScope.() -> Unit) {
        state.addNbtFilter(isNegated = true, init)
    }

    final override fun predicate(predicate: Key) {
        state.addPredicateFilter(predicate, isNegated = true)
    }

    final override fun gamemode(mode: GameMode) {
        state.excludeGamemode(mode)
    }
}
