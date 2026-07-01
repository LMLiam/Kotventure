package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

internal open class NegatedSelfEntitySelectorAdapter(
    state: EntitySelectorState,
) : NegatedCommonEntitySelectorAdapter(state),
    NegatedSelfEntitySelectorScope {
    final override fun type(entityType: Key) {
        state.excludeType(entityType.asString())
    }

    final override fun type(entityType: String) {
        state.excludeType(entityType.withDefaultNamespace())
    }

    final override fun typeTag(entityTypeTag: Key) {
        state.excludeTypeTag(entityTypeTag)
    }
}
