package io.github.lmliam.kotventure.core.nbt

/**
 * Creates an [NbtPath] starting from a root key.
 *
 * The key is used verbatim as the first segment, so a pre-formed path string works as a string
 * escape hatch — chaining the indexing operators simply appends to it:
 *
 * ```kotlin
 * // Structured
 * nbtPath("Items")[0]["id"]
 *
 * // Pre-formed string, still chainable
 * nbtPath("Items[0]")["tag"]
 * ```
 */
public fun nbtPath(key: String): NbtPath = NbtPath(listOf(NbtPathNode.Key(key)))

/**
 * Creates an [NbtPath] starting from a root list index (rare — for paths beginning with `[n]`).
 */
public fun nbtPath(index: Int): NbtPath = NbtPath(listOf(NbtPathNode.Index(index)))
