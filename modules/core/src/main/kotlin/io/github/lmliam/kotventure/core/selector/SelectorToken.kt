package io.github.lmliam.kotventure.core.selector

internal fun Char.isAllowedInUnquotedSelectorToken(): Boolean =
    this in '0'..'9' ||
        this in 'A'..'Z' ||
        this in 'a'..'z' ||
        this == '_' ||
        this == '-' ||
        this == '.' ||
        this == '+'
