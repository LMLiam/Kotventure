package io.github.lmliam.kotventure.core.selector

/**
 * A Java Edition entity-selector head.
 */
public enum class EntitySelectorHead(
    public val token: String,
) {
    /** The nearest player (`@p`). */
    NEAREST_PLAYER("@p"),

    /** All players (`@a`). */
    ALL_PLAYERS("@a"),

    /** A random player (`@r`). */
    RANDOM_PLAYER("@r"),

    /** The executing entity (`@s`). */
    SELF("@s"),

    /** All entities (`@e`). */
    ENTITIES("@e"),

    /** The nearest entity (`@n`). */
    NEAREST_ENTITY("@n"),
}
