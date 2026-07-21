package io.github.lmliam.kotventure.core.nbt

/**
 * An immutable NBT path with key, index, all-elements, and compound-filter segments.
 *
 * Create a path with [nbtPath]. Then, use the indexing operators to append segments. Each operator returns a new path
 * and does not change the source path.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathSample
 *
 * For syntax that this API does not cover, give [nbtPath] a preformed path string. The function uses that string
 * without validation or escaping as the first segment.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathVerbatimSample
 */
public class NbtPath internal constructor(
    internal val nodes: List<NbtPathNode>,
) {
    /**
     * Returns a path with [key] appended as a compound-key segment.
     *
     * @sample io.github.lmliam.kotventure.core.nbt.nbtPathKeySample
     */
    public operator fun get(key: String): NbtPath = NbtPath(nodes + NbtPathNode.Key(key))

    /**
     * Returns a path with [index] appended as a list-index segment.
     *
     * @sample io.github.lmliam.kotventure.core.nbt.nbtPathIndexSample
     */
    public operator fun get(index: Int): NbtPath = NbtPath(nodes + NbtPathNode.Index(index))

    /**
     * Returns a path with the [selection] segment appended.
     *
     * @sample io.github.lmliam.kotventure.core.nbt.nbtPathSelectionSample
     */
    public operator fun get(selection: NbtSelection): NbtPath = NbtPath(nodes + selection.node)

    /**
     * Returns this path in Minecraft NBT path syntax.
     */
    public fun asString(): String = renderNodes(nodes)

    /** Returns the same Minecraft NBT path syntax as [asString]. */
    override fun toString(): String = asString()

    /** Returns `true` when [other] contains the same path segments in the same order. */
    override fun equals(other: Any?): Boolean = other is NbtPath && nodes == other.nodes

    /** Returns a hash code for the ordered path segments. */
    override fun hashCode(): Int = nodes.hashCode()
}
