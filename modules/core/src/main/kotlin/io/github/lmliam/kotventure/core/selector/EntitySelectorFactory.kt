package io.github.lmliam.kotventure.core.selector

/**
 * Returns an `@s` selector targeting the executing entity.
 */
public fun self(): EntitySelector = EntitySelector("@s")

/**
 * Builds a `@p` selector targeting the nearest player, with optional arguments.
 *
 * ```kotlin
 * nearestPlayer { distance(atMost(10.0)) }
 * ```
 */
public fun nearestPlayer(init: EntitySelectorScope.() -> Unit = {}): EntitySelector =
    EntitySelectorBuilder("@p").apply(init).build()

/**
 * Builds an `@a` selector targeting all players, with optional arguments.
 *
 * ```kotlin
 * allPlayers { tag("admin") }
 * ```
 */
public fun allPlayers(init: EntitySelectorScope.() -> Unit = {}): EntitySelector =
    EntitySelectorBuilder("@a").apply(init).build()

/**
 * Builds an `@r` selector targeting a random player, with optional arguments.
 */
public fun randomPlayer(init: EntitySelectorScope.() -> Unit = {}): EntitySelector =
    EntitySelectorBuilder("@r").apply(init).build()

/**
 * Builds an `@e` selector targeting all entities, with optional arguments.
 *
 * ```kotlin
 * entities {
 *     type("armor_stand")
 *     distance(atMost(10.0))
 *     sort(nearest)
 *     limit(1)
 *     tag("display")
 * }
 * ```
 */
public fun entities(init: EntitySelectorScope.() -> Unit = {}): EntitySelector =
    EntitySelectorBuilder("@e").apply(init).build()

/**
 * Wraps a raw selector string as an [EntitySelector].
 *
 * Use this escape hatch for complex selector syntax not covered by the builder.
 */
public fun entitySelector(raw: String): EntitySelector = EntitySelector(raw)
