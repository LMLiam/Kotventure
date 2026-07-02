package io.github.lmliam.kotventure.core.selector

import net.kyori.adventure.key.Key

/**
 * One advancement entry in an `advancements={...}` argument.
 *
 * @property advancement advancement key
 * @property progress whole-advancement or criterion-level requirement
 */
public data class SelectorAdvancementRequirement(
    public val advancement: Key,
    public val progress: SelectorAdvancementProgress,
)

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

    /** Criterion-level completion. */
    public class Criteria(
        criteria: Collection<SelectorAdvancementCriterion>,
    ) : SelectorAdvancementProgress {
        /** Criterion requirements in source order. */
        public val criteria: List<SelectorAdvancementCriterion> =
            buildList(criteria.size) {
                addAll(criteria)
            }

        /** Returns criterion progress with the supplied requirements. */
        public fun copy(criteria: Collection<SelectorAdvancementCriterion> = this.criteria): Criteria =
            Criteria(criteria)

        public override fun equals(other: Any?): Boolean = other is Criteria && criteria == other.criteria

        public override fun hashCode(): Int = criteria.hashCode()

        public override fun toString(): String = "Criteria(criteria=$criteria)"
    }
}

/**
 * One criterion entry in criterion-level advancement progress.
 *
 * @property name non-empty vanilla unquoted-token advancement criterion name
 * @property completed required completion state
 * @throws IllegalArgumentException if [name] is not a valid unquoted token
 */
public data class SelectorAdvancementCriterion(
    public val name: String,
    public val completed: Boolean,
) {
    init {
        require(name.isNotEmpty() && name.all(Char::isAllowedInUnquotedSelectorToken)) {
            "Advancement criterion '$name' contains characters outside vanilla's unquoted-token syntax."
        }
    }
}
