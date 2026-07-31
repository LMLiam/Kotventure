package io.github.lmliam.kotventure.core.replacement

import net.kyori.adventure.text.PatternReplacementResult

/**
 * The outcome for one match inside a [ReplaceScope.condition] block.
 *
 * A block returns this value. Access it through the scoped constants [ConditionScope.replace],
 * [ConditionScope.skip], and [ConditionScope.stop]. Do not construct an entry directly.
 */
public enum class MatchAction(
    internal val result: PatternReplacementResult,
) {
    /** Replaces the current match. */
    REPLACE(PatternReplacementResult.REPLACE),

    /** Skips the current match. The pass keeps searching for later matches. */
    SKIP(PatternReplacementResult.CONTINUE),

    /** Stops the pass. The operation does not change the remaining children. */
    STOP(PatternReplacementResult.STOP),
}
