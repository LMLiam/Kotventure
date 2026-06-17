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
    val path = resolveSnapshotWritePath(name)
    path.parent?.createDirectories()
    path.writeText(content.normalizeSnapshot() + "\n")
}

/** Locates the snapshot named [name] on the classpath, or `null` when it is absent. */
internal fun resolveSnapshotResource(name: String): URL? =
    SnapshotConfig::class.java.getResource("$RESOURCE_PREFIX$name$RESOURCE_SUFFIX")

/**
 * Resolves where the snapshot named [name] should be written in record mode.
 *
 * Resolution order: an explicit [SnapshotConfig.outputDir] wins; otherwise an existing classpath resource is mapped
 * from its `build/resources/test` copy back onto the `src/test/resources` source so updates land in version control;
 * otherwise a brand-new snapshot is placed under the running module's `src/test/resources/snapshots` (Gradle runs tests
 * with the module directory as the working directory).
 */
internal fun resolveSnapshotWritePath(name: String): Path {
    SnapshotConfig.outputDir?.let { dir -> return overridePath(dir, name) }

    resolveSnapshotResource(name)
        ?.takeIf { it.protocol == "file" }
        ?.let { url ->
            sourceSnapshotDir(Path.of(url.toURI()))?.let { dir -> return dir.resolve("$name$RESOURCE_SUFFIX") }
        }

    return defaultSourceSnapshotDir().resolve("$name$RESOURCE_SUFFIX")
}

/**
 * Maps a `…/build/resources/test/snapshots/<file>` resource path onto its `…/src/test/resources/snapshots` source
 * directory, or returns `null` when [resource] is not under the standard Gradle output tree.
 *
 * Operates on [Path] segments — never on a raw URI string — so it is correct on Windows (where `URI.getPath()` yields a
 * leading-slash, drive-prefixed string that is not a valid filesystem path).
 */
internal fun sourceSnapshotDir(resource: Path): Path? {
    val count = resource.nameCount
    if (count <= BUILD_RESOURCE_SEGMENTS.size) return null

    val start = count - 1 - BUILD_RESOURCE_SEGMENTS.size // index of "build"; the file occupies the last segment
    val actual = (0 until BUILD_RESOURCE_SEGMENTS.size).map { resource.getName(start + it).toString() }
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
): Path = Path.of(dir, "$name$RESOURCE_SUFFIX")
