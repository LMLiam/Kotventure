package io.github.lmliam.kotventure.core.selector

/**
 * A selector element that vanilla syntax can prefix-negate with `!`.
 */
public interface SelectorNegatable {
    /** Whether the element excludes matching values instead of requiring them. */
    public val isNegated: Boolean
}
