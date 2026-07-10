package io.github.lmliam.kotventure.core.selector

/**
 * An immutable, structured entity selector such as `@s`, `@p`, or
 * `@e[type=minecraft:armor_stand,limit=1]`.
 *
 * Construct through a typed target factory such as [entities], or validate selector source with
 * [parseSelector].
 *
 * Package orientation: **heads** ([EntitySelectorHead] + factories) narrow a capability **scope**;
 * [EntitySelectorBuilder] is the single mutable backend (singleton slots fail fast; filter groups
 * enforce exclusive/repeatable policy); this type plus sealed [EntitySelectorArgument] form the
 * immutable **model**; [parseSelector] and `parsing/` produce the same model from vanilla source;
 * [asString] is the single **render** path for DSL and parse.
 *
 * @property head selector head (determines which arguments are valid)
 * @property arguments arguments in source or DSL rendering order (immutable list)
 * @throws IllegalArgumentException if any argument is incompatible with [head], a singleton
 *   argument name appears more than once, or an exclusive filter group
 *   (`name`/`type`/`gamemode`/`team`) has two positives or mixes a positive with exclusions
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
        val seenSingletons = mutableSetOf<String>()
        val filterPolarity = mutableMapOf<String, FilterPolarityState>()
        for (argument in arguments) {
            val singletonKey = argument.singletonKey
            if (singletonKey != null) {
                require(seenSingletons.add(singletonKey)) {
                    selectorSingletonAlreadySetMessage(singletonKey)
                }
                continue
            }
            requireValidFilterGroupEntry(argument, filterPolarity)
        }
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

/**
 * Running positive/negative flags for one filter-group argument name while validating a list.
 */
private class FilterPolarityState {
    var hasPositive: Boolean = false
    var hasNegative: Boolean = false
}

/**
 * Applies shared exclusive/repeatable policy for one filter-group argument, updating [states].
 *
 * @throws IllegalArgumentException when exclusive policy is violated
 */
private fun requireValidFilterGroupEntry(
    argument: EntitySelectorArgument,
    states: MutableMap<String, FilterPolarityState>,
) {
    val keyword = argument.keyword ?: return
    val policy = keyword.filterPolicy ?: return
    val name = keyword.sourceName
    val state = states.getOrPut(name) { FilterPolarityState() }
    val isExclusion = (argument as EntitySelectorArgument.Negatable).isFilterExclusion
    val violation = policy.violationFor(name, state.hasPositive, state.hasNegative, isExclusion)
    require(violation == null) { violation!! }
    if (isExclusion) state.hasNegative = true else state.hasPositive = true
}
