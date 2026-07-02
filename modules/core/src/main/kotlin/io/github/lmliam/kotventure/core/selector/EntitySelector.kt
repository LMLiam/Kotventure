package io.github.lmliam.kotventure.core.selector

/**
 * An immutable, structured entity selector such as `@s`, `@p`, or
 * `@e[type=minecraft:armor_stand,limit=1]`.
 *
 * Construct through a typed target factory such as [entities], or validate selector source with
 * [entitySelector].
 *
 * @property head selector head
 * @property arguments arguments in source or DSL rendering order
 * @throws IllegalArgumentException if an argument is incompatible with [head]
 */
@ConsistentCopyVisibility
public data class EntitySelector private constructor(
    public val head: EntitySelectorHead,
    public val arguments: List<EntitySelectorArgument>,
) {
    /** Builds a selector from a defensive snapshot of [arguments]. */
    public constructor(
        head: EntitySelectorHead,
        arguments: Collection<EntitySelectorArgument>,
    ) : this(
        head,
        buildList(arguments.size) { addAll(arguments) },
    )

    init {
        arguments.forEach(head::requireSupportFor)
    }

    /** Renders this selector as canonical selector source. */
    public fun asString(): String =
        if (arguments.isEmpty()) {
            head.token
        } else {
            head.token + arguments.joinToString(",", "[", "]", transform = EntitySelectorArgument::render)
        }

    public override fun toString(): String = asString()
}
