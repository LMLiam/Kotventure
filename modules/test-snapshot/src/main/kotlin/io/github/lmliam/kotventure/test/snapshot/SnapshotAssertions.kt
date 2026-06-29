package io.github.lmliam.kotventure.test.snapshot

import io.kotest.matchers.should
import net.kyori.adventure.text.Component

/**
 * Asserts that this component matches the committed snapshot named [name], returning the receiver for chaining.
 *
 * In normal mode this compares the component against the committed snapshot and fails on a mismatch. In
 * **record mode** — enabled by the `kotventure.snapshot.update` system property or the `SNAPSHOT_UPDATE`
 * environment variable — it instead writes the current component as the snapshot and passes, so a new or
 * intentionally-changed snapshot can be regenerated. Only this assertion writes; [matchSnapshot] never does.
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
