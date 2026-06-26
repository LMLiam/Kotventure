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
}

/**
 * Navigates into a compound key.
 *
 * ```kotlin
 * nbtPath("tag")["display"]["Name"]
 * ```
 */
public operator fun NbtPath.get(key: String): NbtPath {
    val current = repr
    require(current is NbtPathRepr.Structured) { "Cannot chain operators on a raw NbtPath." }
    return NbtPath(NbtPathRepr.Structured(current.nodes + NbtPathNode.Key(key)))
}

/**
 * Navigates into a list element by index.
 *
 * ```kotlin
 * nbtPath("Items")[0]["id"]
 * ```
 */
public operator fun NbtPath.get(index: Int): NbtPath {
    val current = repr
    require(current is NbtPathRepr.Structured) { "Cannot chain operators on a raw NbtPath." }
    return NbtPath(NbtPathRepr.Structured(current.nodes + NbtPathNode.Index(index)))
}

/**
 * Applies a selection (all-elements or compound filter) to the path.
 *
 * ```kotlin
 * nbtPath("Inventory")[all]["id"]
 * nbtPath("Items")[matching { key("id") eq "minecraft:diamond" }]["Count"]
 * ```
 */
public operator fun NbtPath.get(selection: NbtSelection): NbtPath {
    val current = repr
    require(current is NbtPathRepr.Structured) { "Cannot chain operators on a raw NbtPath." }
    return NbtPath(NbtPathRepr.Structured(current.nodes + selection.node))
}

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

private fun containsPathSyntax(s: String): Boolean = s.contains('.') || s.contains('[') || s.contains(']')

/**
 * Creates an [NbtPath] starting from a root key, or wraps a raw path string as an escape hatch.
 *
 * If [key] contains path-syntax characters (`.`, `[`, `]`), it is treated as a raw pre-formed
 * path string. Otherwise it is a structured root key that supports chained indexing:
 *
 * ```kotlin
 * // Structured — chainable
 * nbtPath("Items")[0]["id"]
 *
 * // Raw escape hatch — contains special chars
 * nbtPath("Items[{id:\"minecraft:diamond\"}].Count")
 * ```
 */
public fun nbtPath(key: String): NbtPath =
    if (containsPathSyntax(key)) {
        NbtPath(NbtPathRepr.Raw(key))
    } else {
        NbtPath(NbtPathRepr.Structured(listOf(NbtPathNode.Key(key))))
    }

/**
 * Creates an [NbtPath] starting from a root list index (rare — for paths beginning with `[n]`).
 */
public fun nbtPath(index: Int): NbtPath = NbtPath(NbtPathRepr.Structured(listOf(NbtPathNode.Index(index))))
