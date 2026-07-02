package io.github.lmliam.kotventure.core.selector

/**
 * A named or presence-based condition for selector arguments such as `tag` and `team`.
 */
public sealed interface SelectorStringCondition {
    /**
     * Matches one named value.
     *
     * @property value non-empty vanilla unquoted-token value
     */
    public data class Named(
        public val value: String,
    ) : SelectorStringCondition {
        init {
            require(value.isNotEmpty()) { "Named selector conditions must not be empty." }
            require(value.all(Char::isAllowedInUnquotedSelectorToken)) {
                "Selector condition '$value' contains characters outside vanilla's unquoted-token syntax."
            }
        }
    }

    /**
     * Matches whether any value is present.
     *
     * @property value required presence state
     */
    public data class Presence(
        public val value: SelectorPresence,
    ) : SelectorStringCondition
}
