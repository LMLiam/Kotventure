package io.github.lmliam.kotventure.core.selector

/**
 * One objective entry in a `scores={...}` argument.
 *
 * @property objective non-empty vanilla unquoted-token scoreboard objective name
 * @property range required score range
 * @throws IllegalArgumentException if [objective] is not a valid unquoted token
 */
public data class SelectorScoreRequirement(
    public val objective: String,
    public val range: SelectorIntRange,
) {
    init {
        require(objective.isNotEmpty() && objective.all(Char::isAllowedInUnquotedSelectorToken)) {
            "Score objective '$objective' contains characters outside vanilla's unquoted-token syntax."
        }
    }
}
