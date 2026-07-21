package io.github.lmliam.kotventure.core.selector

/**
 * An immutable named or presence-based condition for a `tag` or `team` selector argument.
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

    /** Converts parsed selector-source values to typed conditions. */
    public companion object {
        /**
         * Converts the vanilla empty-value rule. An empty [value] creates a presence condition. For example, `tag=`
         * requires no tag, and `tag=!` requires a tag. A non-empty [value] creates a named condition.
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
