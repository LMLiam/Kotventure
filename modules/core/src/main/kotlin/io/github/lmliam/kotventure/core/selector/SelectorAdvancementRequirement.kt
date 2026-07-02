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

    /**
     * Criterion-level completion.
     *
     * @property criteria criterion requirements in source order
     */
    @ConsistentCopyVisibility
    public data class Criteria private constructor(
        public val criteria: List<SelectorAdvancementCriterion>,
    ) : SelectorAdvancementProgress {
        /** Builds criterion progress from a defensive snapshot of [criteria]. */
        public constructor(criteria: Collection<SelectorAdvancementCriterion>) : this(
            buildList(criteria.size) { addAll(criteria) },
        )
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
