package io.github.lmliam.kotventure.core.nbt

/**
 * Represents a list selection strategy: all elements or a compound filter.
 */
public class NbtSelection internal constructor(
    internal val node: NbtPathNode,
)

/**
 * Selects all elements in a list (`[]`).
 *
 * ```kotlin
 * nbtPath("Passengers")[all]["CustomName"]
 * ```
 */
public val all: NbtSelection = NbtSelection(NbtPathNode.AllElements)

/**
 * Selects list elements matching a compound predicate (`[{...}]`).
 *
 * ```kotlin
 * nbtPath("Items")[matching { key("id") eq "minecraft:diamond" }]["Count"]
 * ```
 */
public fun matching(init: NbtPredicateScope.() -> Unit): NbtSelection {
    val builder = NbtPredicateBuilder()
    builder.init()
    return NbtSelection(NbtPathNode.MatchingElements(builder.build()))
}
