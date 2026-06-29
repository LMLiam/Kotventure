package io.github.lmliam.kotventure.core.nbt

/**
 * A typed NBT path supporting indexed traversal, all-elements selection, and compound filters.
 *
 * Construct via [nbtPath] and chain the indexing operators:
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathSample
 *
 * For syntax not covered by the typed API, pass a pre-formed path string to [nbtPath]; it is used
 * verbatim as the first segment:
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

    override fun toString(): String = asString()

    override fun equals(other: Any?): Boolean = other is NbtPath && nodes == other.nodes

    override fun hashCode(): Int = nodes.hashCode()
}
