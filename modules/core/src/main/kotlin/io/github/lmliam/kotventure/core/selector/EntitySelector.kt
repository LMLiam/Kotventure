package io.github.lmliam.kotventure.core.selector

/**
 * An immutable, structured entity selector such as `@s`, `@p`, or
 * `@e[type=minecraft:armor_stand,limit=1]`.
 *
 * Construct through a typed target factory such as [entities], or validate selector source with
 * [entitySelector].
 *
 * [hasExplicitArgumentList] distinguishes `@e[]` from `@e`.
 *
 * @property head selector head
 * @property hasExplicitArgumentList whether this selector renders square brackets
 */
public class EntitySelector(
    public val head: EntitySelectorHead,
    arguments: Collection<EntitySelectorArgument>,
    hasExplicitArgumentList: Boolean = arguments.isNotEmpty(),
) {
    /** Arguments in source or DSL rendering order. */
    public val arguments: List<EntitySelectorArgument> = arguments.immutableSnapshot()

    /** Whether this selector renders an explicit square-bracket argument list. */
    public val hasExplicitArgumentList: Boolean = hasExplicitArgumentList || this.arguments.isNotEmpty()

    /** Returns a selector with the supplied immutable state. */
    public fun copy(
        head: EntitySelectorHead = this.head,
        arguments: Collection<EntitySelectorArgument> = this.arguments,
        hasExplicitArgumentList: Boolean = this.hasExplicitArgumentList,
    ): EntitySelector = EntitySelector(head, arguments, hasExplicitArgumentList)

    /** Renders this selector as canonical selector source. */
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
        other is EntitySelector &&
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
