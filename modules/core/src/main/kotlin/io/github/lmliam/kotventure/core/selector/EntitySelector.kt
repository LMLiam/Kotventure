package io.github.lmliam.kotventure.core.selector

/**
 * An immutable, structured entity selector such as `@s`, `@p`, or
 * `@e[type=minecraft:armor_stand,limit=1]`.
 *
 * Construct a selector through a typed target factory such as [entities]. Use [parseSelector] to validate selector
 * source.
 *
 * Package structure: [EntitySelectorHead] and its factories limit the capability scope. [EntitySelectorBuilder] is the
 * one mutable backend. Its singleton slots fail immediately, and its filter groups enforce repetition policy. This
 * type and sealed [EntitySelectorArgument] types form the immutable model. [parseSelector] and `parsing/` produce the
 * same model from vanilla source. [asString] is the one render path for DSL and parsed selectors.
 *
 * @property head selector head (determines which arguments are valid)
 * @property arguments arguments in source or DSL rendering order (immutable list)
 * @throws IllegalArgumentException if an argument is incompatible with [head], a singleton argument occurs more than
 *   one time, or an exclusive filter group has an invalid combination.
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
        arguments.forEach(head::requireSupportFor)
        val occurrences = SelectorArgumentOccurrences()
        arguments
            .firstNotNullOfOrNull { occurrences.recordName(it.argumentName) ?: occurrences.recordFilter(it) }
            ?.let { throw IllegalArgumentException(it) }
    }

    /**
     * Renders this selector as canonical entity-selector source text.
     *
     * Produces `@<head>` for no arguments. Produces `@<head>[arg1,arg2,...]` when arguments are present.
     */
    public fun asString(): String =
        if (arguments.isEmpty()) {
            head.token
        } else {
            head.token + arguments.joinToString(",", "[", "]", transform = EntitySelectorArgument::render)
        }

    /** Same canonical rendering as [asString]. */
    public override fun toString(): String = asString()
}
