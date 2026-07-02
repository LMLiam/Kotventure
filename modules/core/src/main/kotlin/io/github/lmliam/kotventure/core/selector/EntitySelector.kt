package io.github.lmliam.kotventure.core.selector

/**
 * An immutable, structured entity selector such as `@s`, `@p`, or
 * `@e[type=minecraft:armor_stand,limit=1]`.
 *
 * Construct through a typed target factory such as [entities], or validate selector source with
 * [entitySelector].
 *
 * @property head selector head
 * @throws IllegalArgumentException if an argument is incompatible with [head]
 */
public class EntitySelector(
    public val head: EntitySelectorHead,
    arguments: Collection<EntitySelectorArgument>,
) {
    /** Arguments in source or DSL rendering order. */
    public val arguments: List<EntitySelectorArgument> =
        buildList(arguments.size) {
            addAll(arguments)
        }

    init {
        this.arguments.forEach(head::requireSupportFor)
    }

    /** Returns a selector with the supplied immutable state. */
    public fun copy(
        head: EntitySelectorHead = this.head,
        arguments: Collection<EntitySelectorArgument> = this.arguments,
    ): EntitySelector = EntitySelector(head, arguments)

    /** Renders this selector as canonical selector source. */
    public fun asString(): String =
        if (arguments.isEmpty()) {
            head.token
        } else {
            head.token + arguments.joinToString(",", "[", "]", transform = EntitySelectorArgument::render)
        }

    public override fun equals(other: Any?): Boolean =
        other is EntitySelector &&
                head == other.head &&
                arguments == other.arguments

    public override fun hashCode(): Int = 31 * head.hashCode() + arguments.hashCode()

    public override fun toString(): String = asString()
}
