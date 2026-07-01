package io.github.lmliam.kotventure.core.selector

/**
 * Presence condition for scoreboard tag selector arguments.
 *
 * Access via the scoped [CommonEntitySelectorScope.any] and [CommonEntitySelectorScope.none] constants.
 */
public enum class SelectorPresence(
    internal val value: String,
) {
    /** Requires at least one value to be present. */
    ANY("!"),

    /** Requires no value to be present. */
    NONE(""),
}
