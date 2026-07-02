package io.github.lmliam.kotventure.core.selector

/**
 * Builds an `@s` selector targeting the executing entity, with optional arguments.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selfSample
 */
public fun self(init: SelfEntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildSelector(EntitySelectorHead.SELF, init)

/**
 * Builds a `@p` selector targeting the nearest player, with optional arguments.
 *
 * @sample io.github.lmliam.kotventure.core.selector.nearestPlayerSample
 */
public fun nearestPlayer(init: PlayerEntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildSelector(EntitySelectorHead.NEAREST_PLAYER, init)

/**
 * Builds an `@a` selector targeting all players, with optional arguments.
 *
 * @sample io.github.lmliam.kotventure.core.selector.allPlayersSample
 */
public fun allPlayers(init: PlayerEntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildSelector(EntitySelectorHead.ALL_PLAYERS, init)

/**
 * Builds an `@r` selector targeting a random player, with optional arguments.
 *
 * @sample io.github.lmliam.kotventure.core.selector.randomPlayerSample
 */
public fun randomPlayer(init: PlayerEntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildSelector(EntitySelectorHead.RANDOM_PLAYER, init)

/**
 * Builds an `@e` selector targeting all entities, with optional arguments.
 *
 * @sample io.github.lmliam.kotventure.core.selector.entitiesSample
 */
public fun entities(init: EntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildSelector(EntitySelectorHead.ENTITIES, init)

/**
 * Builds an `@n` selector targeting the nearest entity, with optional arguments.
 *
 * @sample io.github.lmliam.kotventure.core.selector.nearestEntitySample
 */
public fun nearestEntity(init: EntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildSelector(EntitySelectorHead.NEAREST_ENTITY, init)

/**
 * Wraps a raw selector string as an [EntitySelector].
 *
 * Use this escape hatch for complex selector syntax not covered by the builder.
 */
public fun entitySelector(raw: String): EntitySelector = EntitySelector(raw)

private fun buildSelector(
    head: EntitySelectorHead,
    configure: EntitySelectorScope.() -> Unit,
): EntitySelector {
    val builder = EntitySelectorBuilder()
    builder.configure(configure)
    return ParsedEntitySelector(head, builder.selectorArguments()).asEntitySelector()
}
