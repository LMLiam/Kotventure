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

/** Standard Gradle output path that [resolveSnapshotWritePath] rewrites back onto its source location. */
private const val BUILD_RESOURCES = "/build/resources/test/"

private const val SOURCE_RESOURCES = "/src/test/resources/"

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
 * Resolution order: an explicit [SnapshotConfig.outputDir] wins; otherwise an existing classpath resource is rewritten
 * from its `build/resources/test` copy back onto the `src/test/resources` source so updates land in version control;
 * otherwise a brand-new snapshot is placed under the running module's `src/test/resources/snapshots` (Gradle runs tests
 * with the module directory as the working directory).
 */
internal fun resolveSnapshotWritePath(name: String): Path {
    SnapshotConfig.outputDir?.let { dir -> return overridePath(dir, name) }

    resolveSnapshotResource(name)
        ?.takeIf { it.protocol == "file" }
        ?.let { url -> return Path.of(url.toURI().path.replace(BUILD_RESOURCES, SOURCE_RESOURCES)) }

    return Path
        .of(System.getProperty("user.dir"), "src", "test", "resources", "snapshots")
        .resolve("$name$RESOURCE_SUFFIX")
}

private fun overridePath(
    dir: String,
    name: String,
): Path = Path.of(dir, "$name$RESOURCE_SUFFIX")
