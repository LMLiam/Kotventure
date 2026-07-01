package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

internal class NegatedEntitySelectorAdapter(
    state: EntitySelectorState,
) : NegatedCommonEntitySelectorAdapter(state),
    NegatedEntitySelectorScope {
    override fun type(entityType: Key) {
        state.excludeType(entityType.asString())
    }

    override fun type(entityType: String) {
        state.excludeType(entityType.withDefaultNamespace())
    }

    override fun typeTag(entityTypeTag: Key) {
        state.excludeTypeTag(entityTypeTag)
    }
}
