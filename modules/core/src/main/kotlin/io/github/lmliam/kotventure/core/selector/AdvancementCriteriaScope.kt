package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Declares criterion requirements for one advancement in [SelectorAdvancementsScope].
 *
 * Entries keep declaration order. Each criterion can occur one time.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorAdvancementsSample
 */
@KotventureDslMarker
public sealed interface AdvancementCriteriaScope {
    /**
     * Requires this criterion to be complete when [completed] is `true`, or incomplete when it is `false`.
     *
     * @throws IllegalArgumentException when the criterion name is empty or contains characters outside vanilla's
     * unquoted-token syntax.
     * @throws IllegalStateException when this criterion already has a completion state.
     */
    public infix fun String.eq(completed: Boolean)
}
