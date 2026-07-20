package io.github.lmliam.kotventure.core.nbt

/**
 * Creates an [NbtPath] starting from a root key.
 *
 * The function uses the key without a change as the first segment. Thus, a preformed path string is a string bridge.
 * Index operators append segments to it.
 *
 * @sample io.github.lmliam.kotventure.core.nbt.nbtPathFactorySample
 */
public fun nbtPath(key: String): NbtPath = NbtPath(listOf(NbtPathNode.Key(key)))

/**
 * Creates an [NbtPath] starting from a root list index (rare — for paths beginning with `[n]`).
 */
public fun nbtPath(index: Int): NbtPath = NbtPath(listOf(NbtPathNode.Index(index)))
