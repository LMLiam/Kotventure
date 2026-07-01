package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

internal class SelfEntitySelectorAdapter(
    state: EntitySelectorState,
) : CommonEntitySelectorAdapter(state),
    SelfEntitySelectorScope {
    override fun type(entityType: Key) {
        state.assignType(entityType)
    }

    override fun type(entityType: String) {
        state.assignType(entityType)
    }

    override fun typeTag(entityTypeTag: Key) {
        state.assignTypeTag(entityTypeTag)
    }
}
