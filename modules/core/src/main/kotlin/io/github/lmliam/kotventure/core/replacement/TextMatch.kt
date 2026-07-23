package io.github.lmliam.kotventure.core.replacement

import java.util.regex.MatchResult

/**
 * A snapshot of one pattern match found inside the content of a text component.
 *
 * Adventure supplies a live `java.util.regex.MatchResult` during a replacement or condition callback. That result
 * is only valid during the callback. `TextMatch` copies every exposed value, so it stays valid after the callback
 * returns.
 *
 * [range] is an index range inside the content of the text component that holds the match. It is not an index
 * into the whole message.
 *
 * @sample io.github.lmliam.kotventure.core.replacement.replaceModifySample
 */
public class TextMatch internal constructor(
    result: MatchResult,
    private val namedGroups: Map<String, Int>,
) {
    /** The complete matched text. */
    public val value: String = result.group()

    /** The index range of [value] inside the content of the text component that holds the match. */
    public val range: IntRange = result.start() until result.end()

    /**
     * The captured text for each group in the pattern, indexed from `0`.
     *
     * Index `0` is the complete match. An optional group that did not take part in the match is `null`.
     */
    public val groups: List<String?> = List(result.groupCount() + 1) { index -> result.group(index) }

    /**
     * Returns the captured text for the group at [index].
     *
     * @throws IndexOutOfBoundsException when [index] is not a group in the match.
     */
    public operator fun get(index: Int): String? = groups[index]

    /**
     * Returns the captured text for the group named [name].
     *
     * @throws IllegalArgumentException when the pattern has no group named [name].
     */
    public operator fun get(name: String): String? {
        val index = requireNotNull(namedGroups[name]) { "The pattern has no group named '$name'." }
        return groups[index]
    }
}
