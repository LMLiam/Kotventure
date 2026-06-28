package io.github.lmliam.kotventure.core.nbt

/**
 * A typed NBT path supporting indexed traversal, all-elements selection, and compound filters.
 *
 * Construct via [nbtPath] and chain the indexing operators:
 * ```kotlin
 * nbtPath("Items")[0]["tag"]["display"]["Name"]
 * nbtPath("Inventory")[all]["id"]
 * nbtPath("Items")[matching { "id" eq "minecraft:diamond" }]["Count"]
 * ```
 *
 * For syntax not covered by the typed API, pass a pre-formed path string to [nbtPath]; it is used
 * verbatim as the first segment:
 * ```kotlin
 * nbtPath("Items[{id:\"minecraft:diamond\"}].Count")
 * ```
 */
public class NbtPath internal constructor(
    internal val nodes: List<NbtPathNode>,
) {
    /**
     * Navigates into a compound key.
     *
     * ```kotlin
     * nbtPath("tag")["display"]["Name"]
     * ```
     */
    public operator fun get(key: String): NbtPath = NbtPath(nodes + NbtPathNode.Key(key))

    /**
     * Navigates into a list element by index.
     *
     * ```kotlin
     * nbtPath("Items")[0]["id"]
     * ```
     */
    public operator fun get(index: Int): NbtPath = NbtPath(nodes + NbtPathNode.Index(index))

    /**
     * Applies an [all]-elements or [matching] compound-filter selection.
     *
     * ```kotlin
     * nbtPath("Inventory")[all]["id"]
     * nbtPath("Items")[matching { "id" eq "minecraft:diamond" }]["Count"]
     * ```
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
