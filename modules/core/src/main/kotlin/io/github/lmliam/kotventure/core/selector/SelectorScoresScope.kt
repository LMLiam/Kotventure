package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * The objective-to-range entries of one vanilla `scores={...}` selector argument.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorScoreSample
 */
@KotventureDslMarker
public sealed interface SelectorScoresScope {
    /**
     * Requires this objective's value to fall within [range]: `"kills" eq atLeast(10)`.
     * Negative bounds are valid.
     *
     * @throws IllegalArgumentException if the objective name is empty or contains characters
     *   outside vanilla's unquoted-token syntax
     * @throws IllegalStateException if this objective already has a range
     */
    public infix fun String.eq(range: SelectorIntRange)

    /**
     * Requires this objective's value to fall within a Kotlin [IntRange]: `"deaths" eq 0..5`.
     *
     * @throws IllegalArgumentException if the range is descending or the objective name is invalid
     * @throws IllegalStateException if this objective already has a range
     */
    public infix fun String.eq(range: IntRange)
}
