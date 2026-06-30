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
 * @sample io.github.lmliam.kotventure.core.nbt.allSample
 */
public val all: NbtSelection = NbtSelection(NbtPathNode.AllElements)

/**
 * Selects list elements matching a compound filter (`[{...}]`).
 *
 * @sample io.github.lmliam.kotventure.core.nbt.matchingSample
 */
public fun matching(init: NbtCompoundScope.() -> Unit): NbtSelection {
    val compound = NbtCompoundBuilder().apply(init).build()
    return NbtSelection(NbtPathNode.MatchingElements(compound))
}
