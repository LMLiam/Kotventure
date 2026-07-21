package io.github.lmliam.kotventure.test.snapshot

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import net.kyori.adventure.text.Component

private fun recordHint(verb: String): String =
    "Re-run with ${SnapshotConfig.UPDATE_ENV}=true (or -D${SnapshotConfig.UPDATE_PROPERTY}=true) to $verb it."

/**
 * Returns a Kotest matcher for the snapshot named [name].
 *
 * The matcher never writes a file, including in update mode. You can compose or negate it without
 * a write side effect. A failed result includes the expected and actual JSON and update instructions.
 *
 * The matcher validates [name] when Kotest evaluates it.
 */
public fun matchSnapshot(name: String): Matcher<Component> = matchSnapshot(name, compact = false)

/**
 * Returns a Kotest matcher that compares the compacted component with snapshot [name].
 *
 * The matcher has the same read-only contract as [matchSnapshot]. Adventure compacts the component
 * before the comparison.
 */
public fun matchCompactedSnapshot(name: String): Matcher<Component> = matchSnapshot(name, compact = true)

internal fun matchSnapshot(
    name: String,
    compact: Boolean,
): Matcher<Component> =
    Matcher { value ->
        val actual = value.toSnapshotJson(compact).normalizeSnapshot()
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
