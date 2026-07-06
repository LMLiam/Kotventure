package io.github.lmliam.kotventure.core.selector

/** The vanilla prefix that negates a selector filter value, as in `tag=!hidden`. */
internal const val SELECTOR_NEGATION_PREFIX: Char = '!'

/**
 * Whether this character is legal in a vanilla unquoted selector-argument token
 * (Brigadier's unquoted-string charset).
 */
internal fun Char.isAllowedInUnquotedSelectorToken(): Boolean =
    this in '0'..'9' ||
            this in 'A'..'Z' ||
            this in 'a'..'z' ||
            this == '_' ||
            this == '-' ||
            this == '.' ||
            this == '+'
