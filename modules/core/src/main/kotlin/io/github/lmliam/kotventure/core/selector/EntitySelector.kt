package io.github.lmliam.kotventure.core.selector

/**
 * An immutable, structured entity selector such as `@s`, `@p`, or
 * `@e[type=minecraft:armor_stand,limit=1]`.
 *
 * Construct through a typed target factory such as [entities], or validate selector source with
 * [parseSelector].
 *
 * @property head selector head (determines which arguments are valid)
 * @property arguments arguments in source or DSL rendering order (immutable list)
 * @throws IllegalArgumentException if any argument is incompatible with [head]
 */
@ConsistentCopyVisibility
public data class EntitySelector private constructor(
    public val head: EntitySelectorHead,
    public val arguments: List<EntitySelectorArgument>,
) {
    /**
     * Builds a selector from a defensive immutable snapshot of [arguments].
     *
     * The provided collection is copied to prevent external mutation.
     */
    public constructor(
        head: EntitySelectorHead,
        arguments: Collection<EntitySelectorArgument>,
    ) : this(head, arguments.toList())

    init {
        // Validate that each argument is supported by this selector head
        arguments.forEach(head::requireSupportFor)
    }

    /**
     * Renders this selector as canonical entity-selector source text.
     *
     * Produces `@<head>` for empty arguments, or `@<head>[arg1,arg2,...]` for non-empty.
     */
    public fun asString(): String =
        if (arguments.isEmpty()) {
            head.token
        } else {
            head.token + arguments.joinToString(",", "[", "]", transform = EntitySelectorArgument::render)
        }

    public override fun toString(): String = asString()
}
