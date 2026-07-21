package io.github.lmliam.kotventure.core.nbt

/**
 * An immutable NBT path segment that selects list elements.
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
 * Creates a selection for list elements that match the compound filter from [init].
 *
 * @throws IllegalStateException when [init] sets the same key more than one time.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.matchingSample
 */
public fun matching(init: NbtCompoundScope.() -> Unit): NbtSelection {
    val compound = NbtCompoundBuilder().apply(init).build()
    return NbtSelection(NbtPathNode.MatchingElements(compound))
}
