package io.github.lmliam.kotventure.core.nbt

/**
 * A typed NBT path supporting indexed traversal, all-elements selection, and compound filters.
 *
 * Construct via [nbtPath] and chain the indexing operators:
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathSample
 *
 * For syntax that the typed API does not cover, give [nbtPath] a preformed path string. The function uses it without a
 * change as the first segment:
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathVerbatimSample
 */
public class NbtPath internal constructor(
    internal val nodes: List<NbtPathNode>,
) {
    /**
     * Navigates into a compound key.
     *
     * @sample io.github.lmliam.kotventure.core.nbt.nbtPathKeySample
     */
    public operator fun get(key: String): NbtPath = NbtPath(nodes + NbtPathNode.Key(key))

    /**
     * Navigates into a list element by index.
     *
     * @sample io.github.lmliam.kotventure.core.nbt.nbtPathIndexSample
     */
    public operator fun get(index: Int): NbtPath = NbtPath(nodes + NbtPathNode.Index(index))

    /**
     * Applies an [all]-elements or [matching] compound-filter selection.
     *
     * @sample io.github.lmliam.kotventure.core.nbt.nbtPathSelectionSample
     */
    public operator fun get(selection: NbtSelection): NbtPath = NbtPath(nodes + selection.node)

    /**
     * Renders this path to Minecraft NBT path syntax for handoff to Adventure.
     */
    public fun asString(): String = renderNodes(nodes)

    /** Same rendering as [asString]. */
    override fun toString(): String = asString()

    /** Value equality over the node list. */
    override fun equals(other: Any?): Boolean = other is NbtPath && nodes == other.nodes

    /** Consistent with [equals]: derived from the node list. */
    override fun hashCode(): Int = nodes.hashCode()
}
