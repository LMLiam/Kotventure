package io.github.lmliam.kotventure.core.nbt

/**
 * Creates an [NbtPath] that starts with [key].
 *
 * The function does not validate or escape [key]. You can therefore supply a preformed path as the first segment.
 * Index operators append segments to that string.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathFactorySample
 */
public fun nbtPath(key: String): NbtPath = NbtPath(listOf(NbtPathNode.Key(key)))

/**
 * Creates an [NbtPath] that starts with the root list [index].
 */
public fun nbtPath(index: Int): NbtPath = NbtPath(listOf(NbtPathNode.Index(index)))
