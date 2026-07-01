package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

internal class EntitySelectorAdapter(
    state: EntitySelectorState,
) : PlayerEntitySelectorAdapter(state),
    EntitySelectorScope {
    override fun type(entityType: Key) {
        state.assignType(entityType)
    }

    override fun type(entityType: String) {
        state.assignType(entityType)
    }
}
