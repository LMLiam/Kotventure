package io.github.lmliam.kotventure.core.replacement

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Decides the outcome for one match inside a [ReplaceScope.condition] block.
 *
 * Return one of the scoped constants [replace], [skip], or [stop] from the block.
 *
 * @sample io.github.lmliam.kotventure.core.replacement.replaceConditionSample
 */
@KotventureDslMarker
public interface ConditionScope {
    /** The snapshotted current match. */
    public val match: TextMatch

    /** The number of matches found so far, including this one. */
    public val matchCount: Int

    /** The number of matches already replaced before this one. */
    public val replacementCount: Int

    /** Replaces this match. */
    public val replace: MatchAction

    /** Skips this match. The pass keeps searching for later matches. */
    public val skip: MatchAction

    /** Stops the pass. The operation does not change the remaining children. */
    public val stop: MatchAction
}
