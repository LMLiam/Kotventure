package io.github.lmliam.kotventure.core.selector

/** One advancement's requirement: whole-advancement completion or per-criterion states. */
internal sealed interface AdvancementCondition {
    @JvmInline
    value class Completed(
        val completed: Boolean,
    ) : AdvancementCondition

    @JvmInline
    value class Criteria(
        val criteria: Map<String, Boolean>,
    ) : AdvancementCondition
}
