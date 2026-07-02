package io.github.lmliam.kotventure.core.selector

/** The mutable builder behind one advancement's criterion block, keyed in declaration order. */
internal class AdvancementCriteriaBuilder : AdvancementCriteriaScope {
    val criteria: Map<String, Boolean>
        field = mutableMapOf()

    override infix fun String.eq(completed: Boolean) {
        val criterion = validCriterionName(this)
        check(criterion !in criteria) {
            "Advancement criterion '$criterion' is already set; vanilla syntax evaluates one state per criterion."
        }
        criteria[criterion] = completed
    }

    private fun validCriterionName(criterion: String): String {
        require(criterion.isNotEmpty()) { "Advancement criterion name must not be empty." }
        require(criterion.all { it.isAllowedInUnquotedSelectorToken() }) {
            "Advancement criterion name '$criterion' contains characters outside vanilla's unquoted-token syntax."
        }
        return criterion
    }
}
