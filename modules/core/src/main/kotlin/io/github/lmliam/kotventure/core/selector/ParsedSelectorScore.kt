package io.github.lmliam.kotventure.core.selector

/**
 * One objective entry in a parsed `scores={...}` argument.
 *
 * @property objective scoreboard objective name
 * @property range required score range
 */
public data class ParsedSelectorScore(
    public val objective: String,
    public val range: SelectorIntRange,
)
