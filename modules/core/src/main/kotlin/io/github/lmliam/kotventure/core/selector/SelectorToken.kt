package io.github.lmliam.kotventure.core.selector

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
