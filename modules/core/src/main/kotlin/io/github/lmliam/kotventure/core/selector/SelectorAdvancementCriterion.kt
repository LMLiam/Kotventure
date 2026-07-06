package io.github.lmliam.kotventure.core.selector

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
