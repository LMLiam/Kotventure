package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * The criterion-to-completion entries of one advancement condition inside
 * [SelectorAdvancementsScope].
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorAdvancementsSample
 */
@KotventureDslMarker
public sealed interface AdvancementCriteriaScope {
    /**
     * Requires this criterion to be complete (`true`) or incomplete (`false`):
     * `"kill_dragon" eq true`.
     *
     * @throws IllegalArgumentException if the criterion name is empty or contains characters
     *   outside vanilla's unquoted-token syntax
     * @throws IllegalStateException if this criterion already has a completion state
     */
    public infix fun String.eq(completed: Boolean)
}
