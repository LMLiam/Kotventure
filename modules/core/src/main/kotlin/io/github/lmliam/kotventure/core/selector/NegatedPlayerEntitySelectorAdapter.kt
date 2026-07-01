package io.github.lmliam.kotventure.core.selector

internal class NegatedPlayerEntitySelectorAdapter(
    state: EntitySelectorState,
) : NegatedCommonEntitySelectorAdapter(state),
    NegatedPlayerEntitySelectorScope
