package io.github.lmliam.kotventure.core.selector

/**
 * Builds an `@s` selector targeting the executing entity, with optional arguments.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selfSample
 */
@JvmOverloads
public fun self(init: SelfEntitySelectorScope.() -> Unit = {}): EntitySelector = buildSelfSelector("@s", init)

/**
 * Builds a `@p` selector targeting the nearest player, with optional arguments.
 *
 * @sample io.github.lmliam.kotventure.core.selector.nearestPlayerSample
 */
public fun nearestPlayer(init: PlayerEntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildPlayerSelector("@p", init)

/**
 * Builds an `@a` selector targeting all players, with optional arguments.
 *
 * @sample io.github.lmliam.kotventure.core.selector.allPlayersSample
 */
public fun allPlayers(init: PlayerEntitySelectorScope.() -> Unit = {}): EntitySelector = buildPlayerSelector("@a", init)

/**
 * Builds an `@r` selector targeting a random player, with optional arguments.
 *
 * @sample io.github.lmliam.kotventure.core.selector.randomPlayerSample
 */
public fun randomPlayer(init: PlayerEntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildPlayerSelector("@r", init)

/**
 * Builds an `@e` selector targeting all entities, with optional arguments.
 *
 * @sample io.github.lmliam.kotventure.core.selector.entitiesSample
 */
public fun entities(init: EntitySelectorScope.() -> Unit = {}): EntitySelector = buildEntitySelector("@e", init)

/**
 * Builds an `@n` selector targeting the nearest entity, with optional arguments.
 *
 * @sample io.github.lmliam.kotventure.core.selector.nearestEntitySample
 */
public fun nearestEntity(init: EntitySelectorScope.() -> Unit = {}): EntitySelector = buildEntitySelector("@n", init)

/**
 * Wraps a raw selector string as an [EntitySelector].
 *
 * Use this escape hatch for complex selector syntax not covered by the builder.
 */
public fun entitySelector(raw: String): EntitySelector = EntitySelector(raw)

private fun buildPlayerSelector(
    head: String,
    init: PlayerEntitySelectorScope.() -> Unit,
): EntitySelector {
    val state = EntitySelectorState()
    PlayerEntitySelectorAdapter(state).init()
    return EntitySelectorRenderer.render(head, state)
}

private fun buildSelfSelector(
    head: String,
    init: SelfEntitySelectorScope.() -> Unit,
): EntitySelector {
    val state = EntitySelectorState()
    SelfEntitySelectorAdapter(state).init()
    return EntitySelectorRenderer.render(head, state)
}

private fun buildEntitySelector(
    head: String,
    init: EntitySelectorScope.() -> Unit,
): EntitySelector {
    val state = EntitySelectorState()
    EntitySelectorAdapter(state).init()
    return EntitySelectorRenderer.render(head, state)
}
