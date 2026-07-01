package io.github.lmliam.kotventure.core.selector

internal sealed interface SelectorAdvancementRequirement {
    val rendered: String

    data class Completion(
        val completed: Boolean,
    ) : SelectorAdvancementRequirement {
        override val rendered: String = completed.toString()
    }

    data class Criteria(
        val criteria: Map<String, Boolean>,
    ) : SelectorAdvancementRequirement {
        override val rendered: String =
            criteria.entries.joinToString(",", "{", "}") { (name, completed) -> "$name=$completed" }
    }
}
