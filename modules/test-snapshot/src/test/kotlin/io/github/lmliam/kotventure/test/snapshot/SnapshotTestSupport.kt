package io.github.lmliam.kotventure.test.snapshot

import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

private val snapshotPropertyLock = ReentrantLock()

/**
 * Runs [block] with temporary snapshot system properties.
 *
 * The function restores the previous properties after [block] completes or throws an exception. A
 * [ReentrantLock] isolates these process-wide changes from concurrent tests.
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
