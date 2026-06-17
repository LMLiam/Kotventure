package io.github.lmliam.kotventure.test.snapshot

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component

private const val RECORD_HINT =
    "Re-run with SNAPSHOT_UPDATE=true (or -Dkotventure.snapshot.update=true) to record it."

/**
 * Matches a component against the committed snapshot named [name].
 *
 * The component is normalised and serialised to canonical, pretty-printed JSON (see [toSnapshotJson]) and compared to
 * `/snapshots/<name>.snapshot.json` on the test classpath:
 *
 * - **Match** — the matcher passes.
 * - **Mismatch** — the matcher fails with a message showing the expected and actual JSON.
 * - **Missing** — the matcher fails, telling you to record it.
 *
 * In **record mode** (`SNAPSHOT_UPDATE=true` / `-Dkotventure.snapshot.update=true`) a missing or differing snapshot is
 * written to its source location instead of failing, so intentional changes are captured explicitly and never
 * overwritten by accident. Set `SNAPSHOT_DIR` / `-Dkotventure.snapshot.dir` to relocate snapshots for non-standard
 * layouts.
 *
 * This is an ordinary Kotest [Matcher], so it composes and negates with `and`, `or`, `should`, and `shouldNot`. Prefer
 * the [shouldMatchSnapshot] sugar for the common case.
 */
public fun matchSnapshot(name: String): Matcher<Component> =
    Matcher { value ->
        val actual = value.toSnapshotJson()
        val expected = readSnapshot(name)?.normalizeSnapshot()

        when {
            expected == null && SnapshotConfig.updateMode -> {
                writeSnapshot(name, actual)
                recorded(name)
            }

            expected == null ->
                MatcherResult(
                    false,
                    { "No snapshot recorded for <$name>. $RECORD_HINT\n\nActual:\n$actual" },
                    { "Expected no snapshot to be recorded for <$name>." },
                )

            actual.normalizeSnapshot() == expected -> matched(name)

            SnapshotConfig.updateMode -> {
                writeSnapshot(name, actual)
                recorded(name)
            }

            else ->
                MatcherResult(
                    false,
                    {
                        "Component does not match snapshot <$name>. " +
                            "Re-run with SNAPSHOT_UPDATE=true (or -Dkotventure.snapshot.update=true) to update it." +
                            "\n\nExpected:\n$expected\n\nActual:\n${actual.normalizeSnapshot()}"
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
 * Record intentional changes explicitly — a missing or differing snapshot is only written (never silently) when record
 * mode is on:
 *
 * - `SNAPSHOT_UPDATE=true` (or `-Dkotventure.snapshot.update=true`) records and updates snapshots.
 * - `SNAPSHOT_DIR=/path` (or `-Dkotventure.snapshot.dir=/path`) relocates where snapshots are read and written.
 *
 * Snapshots complement the structural matchers rather than replacing them: reach for a snapshot when a whole message's
 * serialized output is the regression you care about, and for a structural matcher when a single attribute is.
 */
public infix fun Component.shouldMatchSnapshot(name: String): Component =
    apply {
        this should matchSnapshot(name)
    }

private fun recorded(name: String): MatcherResult =
    MatcherResult(
        true,
        { "Recorded snapshot <$name>." },
        { "Expected component not to be recorded as snapshot <$name>." },
    )

private fun matched(name: String): MatcherResult =
    MatcherResult(
        true,
        { "Expected component to match snapshot <$name>." },
        { "Expected component not to match snapshot <$name>, but it did." },
    )
