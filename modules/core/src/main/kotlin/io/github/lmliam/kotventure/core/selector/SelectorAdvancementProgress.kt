package io.github.lmliam.kotventure.core.selector

import kotlin.ConsistentCopyVisibility

/**
 * Whole-advancement or criterion-level progress.
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
         * Builds criterion progress from a defensive snapshot of [criteria].
         */
        public constructor(criteria: Collection<SelectorAdvancementCriterion>) : this(criteria.toList())
    }
}
