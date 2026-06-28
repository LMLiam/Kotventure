package io.github.lmliam.kotventure.core.nbt

private fun containsPathSyntax(s: String): Boolean = '.' in s || '[' in s || ']' in s

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
 * // Raw escape hatch — contains special chars, still chainable
 * nbtPath("Items[0]")["tag"]
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
