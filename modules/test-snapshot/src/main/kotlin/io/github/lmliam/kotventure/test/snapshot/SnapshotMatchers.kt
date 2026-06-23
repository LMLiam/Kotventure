package io.github.lmliam.kotventure.test.snapshot

import io.kotest.matchers.Matcher
import io.kotest.matchers.MatcherResult
import io.kotest.matchers.should
import net.kyori.adventure.text.Component

private fun recordHint(verb: String): String =
    "Re-run with ${SnapshotConfig.UPDATE_ENV}=true (or -D${SnapshotConfig.UPDATE_PROPERTY}=true) to $verb it."

/**
 * Matches a component against the committed snapshot named [name].
 *
 * This matcher never writes, so it is safe to compose and negate. Missing and mismatched snapshots
 * fail with the expected and actual JSON plus record/update instructions.
 */
public fun matchSnapshot(name: String): Matcher<Component> = matchSnapshot(name, compact = false)

/**
 * Matches a compacted component against the committed snapshot named [name].
 */
public fun matchCompactedSnapshot(name: String): Matcher<Component> = matchSnapshot(name, compact = true)

private fun matchSnapshot(
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

/**
 * Asserts that this component matches the committed snapshot named [name].
 *
 * When record mode is enabled, this positive assertion writes missing or updated snapshots before
 * returning the receiver. Composed and negated matcher paths stay side-effect free because only
 * this entry point writes.
 */
public infix fun Component.shouldMatchSnapshot(name: String): Component = assertSnapshot(name, compact = false)

/**
 * Asserts that this component's compacted form matches the committed snapshot named [name].
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
