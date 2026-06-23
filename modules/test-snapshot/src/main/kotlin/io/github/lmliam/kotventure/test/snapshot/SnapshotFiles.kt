package io.github.lmliam.kotventure.test.snapshot

import java.net.URL
import java.nio.file.Path
import kotlin.io.path.createDirectories
import kotlin.io.path.exists
import kotlin.io.path.readText
import kotlin.io.path.writeText

/** Convention for snapshot resources on the classpath: `/snapshots/<name>.snapshot.json`. */
private const val RESOURCE_PREFIX = "/snapshots/"

private const val RESOURCE_SUFFIX = ".snapshot.json"

/** Trailing path segments of a snapshot resource under a standard Gradle test output tree. */
private val BUILD_RESOURCE_SEGMENTS = listOf("build", "resources", "test", "snapshots")

private val SOURCE_RESOURCE_SEGMENTS = listOf("src", "test", "resources", "snapshots")

/** Reads the committed snapshot named [name], or `null` when none has been recorded yet. */
internal fun readSnapshot(name: String): String? {
    requireValidSnapshotName(name)
    SnapshotConfig.outputDir?.let { dir ->
        val path = overridePath(dir, name)
        return if (path.exists()) path.readText() else null
    }
    return resolveSnapshotResource(name)?.readText()
}

/** Writes [content] as the snapshot named [name], creating parent directories as needed. */
internal fun writeSnapshot(
    name: String,
    content: String,
) {
    requireValidSnapshotName(name)
    val path = resolveSnapshotWritePath(name)
    path.parent?.createDirectories()
    path.writeText(content.normalizeSnapshot() + "\n")
}

/** Locates the snapshot named [name] on the classpath, or `null` when it is absent. */
internal fun resolveSnapshotResource(name: String): URL? =
    SnapshotConfig::class.java.getResource("$RESOURCE_PREFIX${requireValidSnapshotName(name)}$RESOURCE_SUFFIX")

/**
 * Resolves where the snapshot named [name] should be written in record mode.
 *
 * An explicit override directory wins; otherwise an existing classpath resource is mapped back to
 * `src/test/resources/snapshots`, and brand-new snapshots default there from the module workdir.
 */
internal fun resolveSnapshotWritePath(name: String): Path {
    requireValidSnapshotName(name)
    SnapshotConfig.outputDir?.let { dir -> return overridePath(dir, name) }

    resolveSnapshotResource(name)
        ?.takeIf { it.protocol == "file" }
        ?.let { url ->
            sourceSnapshotDir(Path.of(url.toURI()))?.let { dir -> return dir.resolve("$name$RESOURCE_SUFFIX") }
        }

    return defaultSourceSnapshotDir().resolve("$name$RESOURCE_SUFFIX")
}

/**
 * Maps a compiled snapshot resource path back to its source snapshot directory.
 *
 * Segment-based path handling keeps the mapping portable across Windows and Unix path shapes.
 */
internal fun sourceSnapshotDir(resource: Path): Path? {
    val count = resource.nameCount
    if (count <= BUILD_RESOURCE_SEGMENTS.size) return null

    val start = count - 1 - BUILD_RESOURCE_SEGMENTS.size // index of "build"; the file occupies the last segment
    val actual = BUILD_RESOURCE_SEGMENTS.indices.map { resource.getName(start + it).toString() }
    if (actual != BUILD_RESOURCE_SEGMENTS) return null

    val root = resource.root ?: Path.of("")
    val moduleRoot = if (start == 0) root else root.resolve(resource.subpath(0, start))
    return SOURCE_RESOURCE_SEGMENTS.fold(moduleRoot) { dir, segment -> dir.resolve(segment) }
}

private fun defaultSourceSnapshotDir(): Path =
    SOURCE_RESOURCE_SEGMENTS.fold(Path.of(System.getProperty("user.dir"))) { dir, segment -> dir.resolve(segment) }

private fun overridePath(
    dir: String,
    name: String,
): Path = Path.of(dir).resolve("$name$RESOURCE_SUFFIX").normalize()

internal fun requireValidSnapshotName(name: String): String {
    require(name.isNotBlank()) { "Snapshot name must not be blank." }
    require('\\' !in name) { "Snapshot name must use '/' as a separator." }
    val path = Path.of(name)
    require(!path.isAbsolute) { "Snapshot name must be relative: $name." }
    require(path.normalize() == path && path.none { it.toString() == ".." }) {
        "Snapshot name must not escape the snapshot directory: $name."
    }
    require(path.all { it.toString().isNotBlank() }) { "Snapshot name must not contain empty path segments: $name." }
    return name
}
