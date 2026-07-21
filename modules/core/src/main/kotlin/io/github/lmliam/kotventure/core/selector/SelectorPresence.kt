package io.github.lmliam.kotventure.core.selector

/**
 * A presence condition for selector arguments such as `tag` and `team`.
 *
 * Access via the scoped [CommonEntitySelectorScope.any] and [CommonEntitySelectorScope.none] constants.
 *
 * @sample io.github.lmliam.kotventure.core.selector.selectorPresenceSample
 */
public enum class SelectorPresence(
    internal val value: String,
) {
    /** Requires at least one value. This value renders as `!` after the equals sign. */
    ANY("!"),

    /** Requires no value. This value renders as an empty string after the equals sign. */
    NONE(""),
}
