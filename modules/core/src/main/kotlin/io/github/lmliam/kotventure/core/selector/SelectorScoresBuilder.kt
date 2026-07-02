package io.github.lmliam.kotventure.core.selector

/** The mutable builder behind one `scores { ... }` block, keyed in declaration order. */
internal class SelectorScoresBuilder : SelectorScoresScope {
    val scores: Map<String, SelectorIntRange>
        field = mutableMapOf()

    override infix fun String.eq(range: SelectorIntRange) {
        val objective = validObjectiveName(this)
        check(objective !in scores) {
            "Selector score objective '$objective' is already set; vanilla syntax evaluates one range per objective."
        }
        scores[objective] = range
    }

    override infix fun String.eq(range: IntRange) {
        eq(closedRange(range))
    }

    private fun validObjectiveName(objective: String): String {
        require(objective.isNotEmpty()) { "Score objective name must not be empty." }
        require(objective.all { it.isAllowedInUnquotedSelectorToken() }) {
            "Score objective name '$objective' contains characters outside vanilla's unquoted-token syntax."
        }
        return objective
    }
}
