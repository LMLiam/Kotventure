package io.github.lmliam.kotventure.core.selector

/**
 * A typed entity selector such as `@s`, `@p`, or `@e[type=armor_stand,limit=1]`.
 *
 * Construct via the target factories ([self], [nearestPlayer], [allPlayers], [randomPlayer],
 * [entities]) or the string escape-hatch [entitySelector].
 */
@JvmInline
public value class EntitySelector(
    private val selector: String,
) {
    /**
     * Returns the underlying selector string for handoff to Adventure.
     */
    public fun asString(): String = selector

    override fun toString(): String = selector
}

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
