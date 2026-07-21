package io.github.lmliam.kotventure.core.selector

import kotlin.ConsistentCopyVisibility

/**
 * A required advancement state, for the complete advancement or for individual criteria.
 */
public sealed interface SelectorAdvancementProgress {
    /**
     * Whole-advancement completion.
     *
     * @property completed required completion state
     */
    public data class Completion(
        public val completed: Boolean,
    ) : SelectorAdvancementProgress

    /**
     * Criterion-level completion.
     *
     * @property criteria criterion requirements in source order
     */
    @ConsistentCopyVisibility
    public data class Criteria private constructor(
        public val criteria: List<SelectorAdvancementCriterion>,
    ) : SelectorAdvancementProgress {
        /**
         * Creates criterion progress from a snapshot of [criteria].
         *
         * The constructor copies the collection and keeps its iteration order.
         */
        public constructor(criteria: Collection<SelectorAdvancementCriterion>) : this(criteria.toList())
    }
}
