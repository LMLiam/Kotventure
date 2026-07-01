package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

internal class EntitySelectorAdapter(
    state: EntitySelectorState,
) : PlayerEntitySelectorAdapter(state),
    EntitySelectorScope {
    override fun type(entityType: Key) {
        state.type = entityType.asString()
    }

    override fun type(entityType: String) {
        state.type = entityType.withDefaultNamespace()
    }
}
