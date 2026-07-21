package io.github.lmliam.kotventure.test.snapshot

import io.kotest.matchers.should
import net.kyori.adventure.text.Component

/**
 * Verifies that this component matches the snapshot named [name].
 *
 * In normal mode, a missing or different snapshot causes an assertion failure. In update mode, this
 * function writes a missing or different snapshot and passes. Enable update mode with the
 * `kotventure.snapshot.update` system property or the `SNAPSHOT_UPDATE` environment variable.
 *
 * This assertion is the only public snapshot API that writes files. [matchSnapshot] never writes a
 * file.
 *
 * @return this component, for chained assertions.
 * @throws IllegalArgumentException when [name] is blank, absolute, contains a backslash or blank
 *   segment, or escapes the snapshot directory.
 * @throws AssertionError when normal mode is active and the snapshot is missing or different.
 * @sample io.github.lmliam.kotventure.test.snapshot.shouldMatchSnapshotSample
 */
public infix fun Component.shouldMatchSnapshot(name: String): Component = assertSnapshot(name, compact = false)

/**
 * Verifies that the compacted form of this component matches the snapshot named [name].
 *
 * Adventure compacts the component before comparison or update. Compaction merges redundant nodes
 * and can make equivalent component trees use the same snapshot.
 *
 * @return this component, for chained assertions.
 * @throws IllegalArgumentException when [name] is not a valid relative snapshot name.
 * @throws AssertionError when normal mode is active and the snapshot is missing or different.
 */
public infix fun Component.shouldMatchCompactedSnapshot(name: String): Component = assertSnapshot(name, compact = true)

private fun Component.assertSnapshot(
    name: String,
    compact: Boolean,
): Component =
    apply {
        if (SnapshotConfig.updateMode) {
            val actual = toSnapshotJson(compact).normalizeSnapshot()
            if (readSnapshot(name)?.normalizeSnapshot() != actual) {
                writeSnapshot(name, actual)
            }
        } else {
            this should matchSnapshot(name, compact)
        }
    }
