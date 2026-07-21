package io.github.lmliam.kotventure.core.selector

/**
 * A selector value that vanilla syntax can negate with a leading `!`.
 */
public interface SelectorNegatable {
    /** Whether the element excludes matching values instead of requiring them. */
    public val isNegated: Boolean
}
