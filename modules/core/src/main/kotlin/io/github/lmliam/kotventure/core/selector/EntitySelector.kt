package io.github.lmliam.kotventure.core.selector

/**
 * An immutable, structured entity selector such as `@s`, `@p`, or
 * `@e[type=minecraft:armor_stand,limit=1]`.
 *
 * Use a typed factory such as [entities] to create a selector. Use [parseSelector] to parse and validate selector
 * source. The constructor copies [arguments], and the selector does not change after construction.
 *
 * @property head The selector head. The head determines which arguments are valid.
 * @property arguments The arguments in source or DSL rendering order.
 * @throws IllegalArgumentException when an argument is incompatible with [head], a singleton argument occurs more
 * than one time, or an exclusive filter group has an invalid combination.
 */
@ConsistentCopyVisibility
public data class EntitySelector private constructor(
    public val head: EntitySelectorHead,
    public val arguments: List<EntitySelectorArgument>,
) {
    /**
     * Creates a selector from a snapshot of [arguments].
     *
     * The constructor copies the collection before it validates the arguments.
     */
    public constructor(
        head: EntitySelectorHead,
        arguments: Collection<EntitySelectorArgument>,
    ) : this(head, arguments.toList())

    init {
        arguments.forEach(head::requireSupportFor)
        val occurrences = SelectorArgumentOccurrences()
        arguments
            .firstNotNullOfOrNull { occurrences.recordName(it.argumentName) ?: occurrences.recordFilter(it) }
            ?.let { throw IllegalArgumentException(it) }
    }

    /**
     * Returns this selector as canonical entity-selector source text.
     *
     * A selector with no arguments renders as its head. Other selectors keep the order of [arguments] and render as
     * `@<head>[arg1,arg2,...]`.
     */
    public fun asString(): String =
        if (arguments.isEmpty()) {
            head.token
        } else {
            head.token + arguments.joinToString(",", "[", "]", transform = EntitySelectorArgument::render)
        }

    /** Returns the same canonical source text as [asString]. */
    public override fun toString(): String = asString()
}
