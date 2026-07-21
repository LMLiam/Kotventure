package io.github.lmliam.kotventure.core.selector

/**
 * Creates an `@s` selector for the executing entity.
 *
 * [init] can use common arguments and entity-type filters. It cannot use `limit` or `sort`.
 *
 * @throws IllegalArgumentException when an argument value is invalid.
 * @throws IllegalStateException when [init] repeats a singleton argument or creates an invalid filter combination.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selfSample
 */
public fun self(init: SelfEntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildSelector(EntitySelectorHead.SELF, init)

/**
 * Creates an `@p` selector for the nearest player.
 *
 * [init] can use common arguments, `limit`, and `sort`. It cannot use entity-type filters.
 *
 * @throws IllegalArgumentException when an argument value is invalid.
 * @throws IllegalStateException when [init] repeats a singleton argument or creates an invalid filter combination.
 *
 * @sample io.github.lmliam.kotventure.core.selector.nearestPlayerSample
 */
public fun nearestPlayer(init: PlayerEntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildSelector(EntitySelectorHead.NEAREST_PLAYER, init)

/**
 * Creates an `@a` selector for all players.
 *
 * [init] can use common arguments, `limit`, and `sort`. It cannot use entity-type filters.
 *
 * @throws IllegalArgumentException when an argument value is invalid.
 * @throws IllegalStateException when [init] repeats a singleton argument or creates an invalid filter combination.
 *
 * @sample io.github.lmliam.kotventure.core.selector.allPlayersSample
 */
public fun allPlayers(init: PlayerEntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildSelector(EntitySelectorHead.ALL_PLAYERS, init)

/**
 * Creates an `@r` selector for a random player.
 *
 * [init] can use common arguments, `limit`, and `sort`. It cannot use entity-type filters.
 *
 * @throws IllegalArgumentException when an argument value is invalid.
 * @throws IllegalStateException when [init] repeats a singleton argument or creates an invalid filter combination.
 *
 * @sample io.github.lmliam.kotventure.core.selector.randomPlayerSample
 */
public fun randomPlayer(init: PlayerEntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildSelector(EntitySelectorHead.RANDOM_PLAYER, init)

/**
 * Creates an `@e` selector for all entities.
 *
 * [init] can use all typed selector arguments.
 *
 * @throws IllegalArgumentException when an argument value is invalid.
 * @throws IllegalStateException when [init] repeats a singleton argument or creates an invalid filter combination.
 *
 * @sample io.github.lmliam.kotventure.core.selector.entitiesSample
 */
public fun entities(init: EntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildSelector(EntitySelectorHead.ENTITIES, init)

/**
 * Creates an `@n` selector for the nearest entity.
 *
 * [init] can use all typed selector arguments. Java Edition treats the default `limit` and `sort` values of `@n` as
 * overridable, so this scope includes both arguments.
 *
 * @throws IllegalArgumentException when an argument value is invalid.
 * @throws IllegalStateException when [init] repeats a singleton argument or creates an invalid filter combination.
 *
 * @sample io.github.lmliam.kotventure.core.selector.nearestEntitySample
 */
public fun nearestEntity(init: EntitySelectorScope.() -> Unit = {}): EntitySelector =
    buildSelector(EntitySelectorHead.NEAREST_ENTITY, init)

private fun buildSelector(
    head: EntitySelectorHead,
    configure: EntitySelectorScope.() -> Unit,
): EntitySelector {
    val builder = EntitySelectorBuilder()
    builder.configure(configure)
    return EntitySelector(head, builder.selectorArguments())
}
