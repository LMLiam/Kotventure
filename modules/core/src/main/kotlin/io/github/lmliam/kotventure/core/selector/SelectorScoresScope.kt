package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Declares score requirements for one vanilla `scores={...}` selector argument.
 *
 * Entries keep declaration order. Each objective can occur one time. Score bounds can be negative.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorScoreSample
 */
@KotventureDslMarker
public sealed interface SelectorScoresScope {
    /**
     * Requires this objective's value to be in [range].
     *
     * @throws IllegalArgumentException when the objective name is empty or contains characters outside vanilla's
     * unquoted-token syntax.
     * @throws IllegalStateException when this objective already has a range.
     */
    public infix fun String.eq(range: SelectorIntRange)

    /**
     * Requires this objective's value to fall within a Kotlin [IntRange]: `"deaths" eq 0..5`.
     *
     * @throws IllegalArgumentException when [range] is descending or the objective name is invalid.
     * @throws IllegalStateException when this objective already has a range.
     */
    public infix fun String.eq(range: IntRange)
}
