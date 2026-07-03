package io.github.lmliam.kotventure.core.selector

/**
 * Java Edition entity-selector heads and the keyword arguments each one accepts.
 *
 * Coordinates and floating-point ranges are handled separately, so this enum only gates keyword
 * arguments.
 *
 * @property token canonical selector source token, such as `@p`
 */
public enum class EntitySelectorHead(
    public val token: String,
    private val supportsTypeFilters: Boolean = true,
    private val supportsLimitAndSort: Boolean = true,
) {
    /** The nearest player (`@p`). */
    NEAREST_PLAYER("@p", supportsTypeFilters = false),

    /** All players (`@a`). */
    ALL_PLAYERS("@a", supportsTypeFilters = false),

    /** A random player (`@r`). */
    RANDOM_PLAYER("@r", supportsTypeFilters = false),

    /** The executing entity (`@s`). */
    SELF("@s", supportsLimitAndSort = false),

    /** All entities (`@e`). */
    ENTITIES("@e"),

    /** The nearest entity (`@n`). */
    NEAREST_ENTITY("@n"),
    ;

    internal fun supports(keyword: SelectorArgumentKeyword): Boolean =
        when (keyword) {
            SelectorArgumentKeyword.TYPE -> supportsTypeFilters
            SelectorArgumentKeyword.LIMIT,
            SelectorArgumentKeyword.SORT,
            -> supportsLimitAndSort
            else -> true
        }

    internal fun requireSupportFor(argument: EntitySelectorArgument) {
        val keyword = argument.keyword ?: return
        require(supports(keyword)) {
            "Selector $token does not support '${keyword.sourceName}'."
        }
    }
}
