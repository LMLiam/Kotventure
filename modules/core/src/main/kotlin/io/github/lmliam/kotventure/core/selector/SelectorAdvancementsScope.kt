package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.key.Key

/**
 * Declares advancement requirements for one vanilla `advancements={...}` selector argument.
 *
 * Entries keep declaration order. Each advancement can occur one time.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorAdvancementsSample
 */
@KotventureDslMarker
public sealed interface SelectorAdvancementsScope {
    /**
     * Requires this advancement to be complete when [completed] is `true`, or incomplete when it is `false`.
     *
     * @throws IllegalStateException when this advancement already has a condition.
     */
    public infix fun Key.eq(completed: Boolean)

    /**
     * Requires the per-criterion completion states that [criteria] declares.
     *
     * An empty block renders as `{}`.
     *
     * @throws IllegalArgumentException when a criterion name is empty or invalid.
     * @throws IllegalStateException when this advancement or a criterion occurs more than one time.
     */
    public infix fun Key.eq(criteria: AdvancementCriteriaScope.() -> Unit)
}
