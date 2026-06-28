package io.github.lmliam.kotventure.core.nbt

/**
 * A typed NBT path supporting indexed traversal, all-elements selection, and compound filters.
 *
 * Construct via [nbtPath] and chain with indexing operators:
 * ```kotlin
 * nbtPath("Items")[0]["tag"]["display"]["Name"]
 * nbtPath("Inventory")[all]["id"]
 * nbtPath("Items")[matching { key("id") eq "minecraft:diamond" }]["Count"]
 * ```
 *
 * For advanced syntax not covered by the typed API, pass a full path string directly:
 * ```kotlin
 * nbtPath("Items[{id:\"minecraft:diamond\"}].Count")
 * ```
 */
public class NbtPath internal constructor(
    internal val repr: NbtPathRepr,
) {
    /**
     * Navigates into a compound key.
     *
     * ```kotlin
     * nbtPath("tag")["display"]["Name"]
     * ```
     */
    public operator fun get(key: String): NbtPath =
        NbtPath(NbtPathRepr.Structured(structuredNodes() + NbtPathNode.Key(key)))

    /**
     * Navigates into a list element by index.
     *
     * ```kotlin
     * nbtPath("Items")[0]["id"]
     * ```
     */
    public operator fun get(index: Int): NbtPath =
        NbtPath(NbtPathRepr.Structured(structuredNodes() + NbtPathNode.Index(index)))

    /**
     * Applies a selection (all-elements or compound filter) to the path.
     *
     * ```kotlin
     * nbtPath("Inventory")[all]["id"]
     * nbtPath("Items")[matching { key("id") eq "minecraft:diamond" }]["Count"]
     * ```
     */
    public operator fun get(selection: NbtSelection): NbtPath =
        NbtPath(NbtPathRepr.Structured(structuredNodes() + selection.node))

    /**
     * Renders this path to Minecraft NBT path syntax for handoff to Adventure.
     */
    public fun asString(): String =
        when (repr) {
            is NbtPathRepr.Raw -> repr.path
            is NbtPathRepr.Structured -> renderNodes(repr.nodes)
        }

    override fun toString(): String = asString()

    override fun equals(other: Any?): Boolean = other is NbtPath && repr == other.repr

    override fun hashCode(): Int = repr.hashCode()

    private fun structuredNodes(): List<NbtPathNode> =
        when (repr) {
            is NbtPathRepr.Structured -> repr.nodes
            is NbtPathRepr.Raw -> listOf(NbtPathNode.Key(repr.path))
        }
}
