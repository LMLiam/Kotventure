package io.github.lmliam.kotventure.core.selector

/**
 * A named or presence-based condition for selector arguments such as `tag` and `team`.
 */
public sealed interface SelectorStringCondition : SelectorNegatable {
    /**
     * Matches one named value.
     *
     * @property value non-empty vanilla unquoted-token value
     * @property isNegated whether the named value is excluded rather than required
     * @throws IllegalArgumentException if [value] is not a valid unquoted token
     */
    public data class Named(
        public val value: String,
        override val isNegated: Boolean = false,
    ) : SelectorStringCondition {
        init {
            require(value.isNotEmpty()) { "Named selector conditions must not be empty." }
            require(value.all(Char::isAllowedInUnquotedSelectorToken)) {
                "Selector condition '$value' contains characters outside vanilla's unquoted-token syntax."
            }
        }
    }

    /**
     * Matches whether any value is present. Presence conditions carry their polarity in [value],
     * so they are never additionally negated.
     *
     * @property value required presence state
     */
    public data class Presence(
        public val value: SelectorPresence,
    ) : SelectorStringCondition {
        override val isNegated: Boolean get() = false
    }

    /** Construction from selector-source semantics. */
    public companion object {
        /**
         * Maps the vanilla empty-value rule. An empty [value] is a presence test. `tag=` requires no value, and `tag=!`
         * requires a value. A non-empty [value] is a named condition.
         */
        internal operator fun invoke(
            value: String,
            isNegated: Boolean,
        ): SelectorStringCondition =
            if (value.isEmpty()) {
                Presence(if (isNegated) SelectorPresence.ANY else SelectorPresence.NONE)
            } else {
                Named(value, isNegated)
            }
    }
}
