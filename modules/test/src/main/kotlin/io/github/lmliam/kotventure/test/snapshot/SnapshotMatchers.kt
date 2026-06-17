package io.github.lmliam.kotventure.test.snapshot

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component

private fun recordHint(verb: String): String =
    "Re-run with SNAPSHOT_UPDATE=true (or -Dkotventure.snapshot.update=true) to $verb it."

/**
 * Matches a component against the committed snapshot named [name].
 *
 * This is a **pure comparison** matcher with no side effects: the component is normalised and serialised to canonical,
 * pretty-printed JSON (see [toSnapshotJson]) and compared to `/snapshots/<name>.snapshot.json` on the test classpath.
 *
 * - **Match** — passes.
 * - **Mismatch** — fails, showing the expected and actual JSON.
 * - **Missing** — fails, telling you to record it.
 *
 * Because it never writes, it is safe to compose and negate with `and`, `or`, `should`, and `shouldNot`. **Recording is
 * deliberately not done here** — a matcher may be evaluated by `shouldNot` or a combinator, where writing would corrupt
 * the very fixture under test. Record mode lives in the positive [shouldMatchSnapshot] path instead; prefer that sugar.
 */
public fun matchSnapshot(name: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.toSnapshotJson().normalizeSnapshot()
        val expected = readSnapshot(name)?.normalizeSnapshot()

        when {
            expected == null ->
                MatcherResult(
                    false,
                    { "No snapshot recorded for <$name>. ${recordHint("record")}\n\nActual:\n$actual" },
                    { "Expected no snapshot to be recorded for <$name>." },
                )

            actual == expected ->
                MatcherResult(
                    true,
                    { "Expected component to match snapshot <$name>." },
                    { "Expected component not to match snapshot <$name>, but it did." },
                )

            else ->
                MatcherResult(
                    false,
                    {
                        "Component does not match snapshot <$name>. ${recordHint("update")}" +
                            "\n\nExpected:\n$expected\n\nActual:\n$actual"
                    },
                    { "Expected component not to match snapshot <$name>." },
                )
        }
    }

/**
 * Asserts that this component matches the committed snapshot named [name], returning the receiver so assertions chain.
 *
 * Serialises the (compacted) component to canonical, pretty-printed JSON and compares it to
 * `/snapshots/<name>.snapshot.json` under the test resources. A mismatch or a missing snapshot fails the test with the
 * offending JSON.
 *
 * This is the **record-aware** entry point — and the only one that writes. Recording is explicit and never silent: a
 * missing or differing snapshot is written (and the assertion passes) only when record mode is on, so it can never be
 * triggered through a negated or composed matcher evaluation:
 *
 * - `SNAPSHOT_UPDATE=true` (or `-Dkotventure.snapshot.update=true`) records and updates snapshots.
 * - `SNAPSHOT_DIR=/path` (or `-Dkotventure.snapshot.dir=/path`) relocates where snapshots are read and written.
 *
 * Snapshots complement the structural matchers rather than replacing them: reach for a snapshot when a whole message's
 * serialized output is the regression you care about, and for a structural matcher when a single attribute is.
 */
public infix fun Component.shouldMatchSnapshot(name: String): Component =
    apply {
        if (SnapshotConfig.updateMode) {
            val actual = toSnapshotJson().normalizeSnapshot()
            if (readSnapshot(name)?.normalizeSnapshot() != actual) {
                writeSnapshot(name, actual)
            }
        } else {
            this should matchSnapshot(name)
        }
    }
