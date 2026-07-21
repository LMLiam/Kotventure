package io.github.lmliam.kotventure.core.selector

/**
 * The supported Java Edition entity-selector heads.
 *
 * Each head limits the keyword arguments that its typed factory exposes. [SELF] does not support `limit` or `sort`.
 * Player heads do not support `type`.
 *
 * @property token The canonical selector source token, such as `@p`.
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
