package io.github.lmliam.kotventure.core.selector

/**
 * An immutable parsed entity selector that can be inspected, copied, transformed, and rendered.
 *
 * [hasExplicitArgumentList] distinguishes `@e[]` from `@e` so valid empty argument lists round trip
 * without loss.
 *
 * @sample io.github.lmliam.kotventure.core.selector.parsedEntitySelectorSample
 */
public data class ParsedEntitySelector(
    public val head: EntitySelectorHead,
    public val arguments: List<EntitySelectorArgument>,
    public val hasExplicitArgumentList: Boolean = arguments.isNotEmpty(),
) {
    /**
     * Renders this model as an [EntitySelector].
     */
    public fun asEntitySelector(): EntitySelector = EntitySelector(asString())

    /**
     * Renders this model as selector source.
     */
    public fun asString(): String {
        val suffix =
            if (!hasExplicitArgumentList && arguments.isEmpty()) {
                ""
            } else {
                arguments.joinToString(",", "[", "]", transform = EntitySelectorArgument::render)
            }
        return "${head.token}$suffix"
    }

    public override fun toString(): String = asString()
}
