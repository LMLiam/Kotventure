package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

/**
 * One advancement entry in a parsed `advancements={...}` argument.
 *
 * @property advancement advancement key
 * @property progress whole-advancement or criterion-level requirement
 */
public data class ParsedSelectorAdvancement(
    public val advancement: Key,
    public val progress: ParsedAdvancementProgress,
)

/**
 * Whole-advancement or criterion-level progress.
 */
public sealed interface ParsedAdvancementProgress {
    /**
     * Whole-advancement completion.
     *
     * @property completed required completion state
     */
    public data class Completion(
        public val completed: Boolean,
    ) : ParsedAdvancementProgress

    /** Criterion-level completion. */
    public class Criteria(
        criteria: Collection<ParsedAdvancementCriterion>,
    ) : ParsedAdvancementProgress {
        /** Criterion requirements in source order. */
        public val criteria: List<ParsedAdvancementCriterion> =
            buildList(criteria.size) {
                addAll(criteria)
            }

        /** Returns criterion progress with the supplied requirements. */
        public fun copy(criteria: Collection<ParsedAdvancementCriterion> = this.criteria): Criteria = Criteria(criteria)

        public override fun equals(other: Any?): Boolean = other is Criteria && criteria == other.criteria

        public override fun hashCode(): Int = criteria.hashCode()

        public override fun toString(): String = "Criteria(criteria=$criteria)"
    }
}

/**
 * One criterion entry in parsed advancement progress.
 *
 * @property name advancement criterion name
 * @property completed required completion state
 */
public data class ParsedAdvancementCriterion(
    public val name: String,
    public val completed: Boolean,
)
