package io.github.lmliam.kotventure.core.selector

internal class NegatedEntitySelectorAdapter(
    state: EntitySelectorState,
) : NegatedSelfEntitySelectorAdapter(state),
    NegatedEntitySelectorScope
