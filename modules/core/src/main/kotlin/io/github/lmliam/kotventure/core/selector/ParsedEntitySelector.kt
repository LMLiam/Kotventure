package io.github.lmliam.kotventure.core.selector

/**
 * An immutable parsed entity selector that can be inspected, copied, transformed, and rendered.
 *
 * [hasExplicitArgumentList] distinguishes `@e[]` from `@e` so valid empty argument lists round trip
 * without loss.
 *
 * @sample io.github.lmliam.kotventure.core.selector.parsedEntitySelectorSample
 */
public class ParsedEntitySelector(
    public val head: EntitySelectorHead,
    arguments: Collection<EntitySelectorArgument>,
    public val hasExplicitArgumentList: Boolean = arguments.isNotEmpty(),
) {
    /** Parsed arguments in source order. */
    public val arguments: List<EntitySelectorArgument> = arguments.immutableSnapshot()

    /**
     * Returns a parsed selector with the supplied immutable state.
     */
    public fun copy(
        head: EntitySelectorHead = this.head,
        arguments: Collection<EntitySelectorArgument> = this.arguments,
        hasExplicitArgumentList: Boolean = this.hasExplicitArgumentList,
    ): ParsedEntitySelector = ParsedEntitySelector(head, arguments, hasExplicitArgumentList)

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

    public override fun equals(other: Any?): Boolean =
        other is ParsedEntitySelector &&
            head == other.head &&
            arguments == other.arguments &&
            hasExplicitArgumentList == other.hasExplicitArgumentList

    public override fun hashCode(): Int {
        var result = head.hashCode()
        result = 31 * result + arguments.hashCode()
        result = 31 * result + hasExplicitArgumentList.hashCode()
        return result
    }

    public override fun toString(): String = asString()
}
