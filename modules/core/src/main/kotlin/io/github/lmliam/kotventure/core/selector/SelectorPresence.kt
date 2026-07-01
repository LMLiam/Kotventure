package io.github.lmliam.kotventure.core.selector

/**
 * Presence condition for selector arguments that test whether a value exists at all, such as
 * `tag` and `team`.
 *
 * Access via the scoped [CommonEntitySelectorScope.any] and [CommonEntitySelectorScope.none] constants.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorPresenceSample
 */
public enum class SelectorPresence(
    internal val value: String,
) {
    /** Requires at least one value to be present. */
    ANY("!"),

    /** Requires no value to be present. */
    NONE(""),
}
