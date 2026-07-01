package io.github.lmliam.kotventure.core.selector

internal class AdvancementCriteriaBuilder : AdvancementCriteriaScope {
    private val criteria: MutableMap<String, Boolean> = linkedMapOf()

    override fun criterion(
        name: String,
        completed: Boolean,
    ) {
        requireValidAdvancementCriterion(name)
        criteria[name] = completed
    }

    fun build(): Map<String, Boolean> = criteria.toMap()
}
