package io.github.lmliam.kotventure.test.snapshot

import io.kotest.matchers.should
import net.kyori.adventure.text.Component

/**
 * Asserts that this component matches the committed snapshot named [name], returning the receiver for chaining.
 *
 * In normal mode, this function compares the component with the committed snapshot and fails on a difference. The
 * `kotventure.snapshot.update` system property or `SNAPSHOT_UPDATE` environment variable enables **record mode**. In
 * record mode, the function writes the current component as the snapshot and passes. Use it to generate a new or
 * intentionally changed snapshot. Only this assertion writes a snapshot. [matchSnapshot] does not write one.
 *
 * @sample io.github.lmliam.kotventure.test.snapshot.shouldMatchSnapshotSample
 */
public infix fun Component.shouldMatchSnapshot(name: String): Component = assertSnapshot(name, compact = false)

/**
 * Asserts that this component's compacted form matches the committed snapshot named [name].
 *
 * Behaves exactly like [shouldMatchSnapshot] — including record mode — but compares the component after
 * Adventure's `compact()` merges redundant nodes, so semantically-equal trees with different shapes still match.
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
