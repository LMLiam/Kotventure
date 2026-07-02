package io.github.lmliam.kotventure.core.selector

/**
 * A Java Edition entity-selector head.
 *
 * The internal capability flags mirror what the vanilla parser accepts per head, matching the
 * sealed selector DSL scopes: player-only heads reject `type`, and the single-target `@s` rejects
 * `limit` and `sort`.
 *
 * @property token canonical selector source token, such as `@p`
 */
public enum class EntitySelectorHead(
    public val token: String,
    internal val acceptsTypeFilters: Boolean = true,
    internal val acceptsResultControls: Boolean = true,
) {
    /** The nearest player (`@p`). */
    NEAREST_PLAYER("@p", acceptsTypeFilters = false),

    /** All players (`@a`). */
    ALL_PLAYERS("@a", acceptsTypeFilters = false),

    /** A random player (`@r`). */
    RANDOM_PLAYER("@r", acceptsTypeFilters = false),

    /** The executing entity (`@s`). */
    SELF("@s", acceptsResultControls = false),

    /** All entities (`@e`). */
    ENTITIES("@e"),

    /** The nearest entity (`@n`). */
    NEAREST_ENTITY("@n"),
}

/**
 * The one head-compatibility policy, keyed by selector-source [name] so the parser can reject an
 * unsupported argument before reading its value.
 */
internal fun EntitySelectorHead.supportsArgument(name: String): Boolean =
    when (name) {
        "type" -> acceptsTypeFilters
        "limit", "sort" -> acceptsResultControls
        else -> true
    }

internal fun EntitySelectorHead.requireSupportFor(argument: EntitySelectorArgument) {
    require(supportsArgument(argument.argumentName)) {
        "Selector $token does not support '${argument.argumentName}'."
    }
}
