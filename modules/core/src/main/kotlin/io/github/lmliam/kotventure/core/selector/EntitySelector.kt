package io.github.lmliam.kotventure.core.selector

/**
 * A typed entity selector such as `@s`, `@p`, or `@e[type=armor_stand,limit=1]`.
 *
 * Construct via the target factories ([self], [nearestPlayer], [allPlayers], [randomPlayer],
 * [entities]) or the string escape-hatch [entitySelector].
 */
@JvmInline
public value class EntitySelector(
    private val selector: String,
) {
    /**
     * Returns the underlying selector string for handoff to Adventure.
     */
    public fun asString(): String = selector

    override fun toString(): String = selector
}
