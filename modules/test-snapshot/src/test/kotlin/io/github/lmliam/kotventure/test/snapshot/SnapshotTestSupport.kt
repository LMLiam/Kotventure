package io.github.lmliam.kotventure.test.snapshot

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val snapshotPropertyLock = ReentrantLock()

/**
 * Runs [block] with the snapshot system properties set to [update]/[dir] (or cleared when their value is the default),
 * restoring whatever values were present before. The save/mutate/restore is held under a [ReentrantLock] so the
 * global-property change stays isolated even if the suite is ever configured to run specs concurrently.
 */
internal fun <T> withSnapshotProperties(
    update: Boolean = false,
    dir: String? = null,
    block: () -> T,
): T =
    snapshotPropertyLock.withLock {
        val previousUpdate = System.getProperty(SnapshotConfig.UPDATE_PROPERTY)
        val previousDir = System.getProperty(SnapshotConfig.DIR_PROPERTY)
        try {
            applyProperty(SnapshotConfig.UPDATE_PROPERTY, if (update) "true" else null)
            applyProperty(SnapshotConfig.DIR_PROPERTY, dir)
            block()
        } finally {
            applyProperty(SnapshotConfig.UPDATE_PROPERTY, previousUpdate)
            applyProperty(SnapshotConfig.DIR_PROPERTY, previousDir)
        }
    }

private fun applyProperty(
    key: String,
    value: String?,
) {
    if (value == null) System.clearProperty(key) else System.setProperty(key, value)
}
