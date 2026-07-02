package io.github.lmliam.kotventure.core.selector

/**
 * Result of opt-in entity-selector parsing.
 */
public sealed interface EntitySelectorParseResult {
    /**
     * A successfully parsed selector.
     *
     * @property selector immutable typed selector model
     */
    public data class Success(
        public val selector: ParsedEntitySelector,
    ) : EntitySelectorParseResult

    /**
     * A selector that could not be parsed without loss.
     *
     * @property error precise parse diagnostic
     */
    public data class Failure(
        public val error: EntitySelectorParseError,
    ) : EntitySelectorParseResult
}

/**
 * A precise entity-selector parse failure.
 *
 * @property offset zero-based Kotlin string index at which parsing failed
 * @property message actionable explanation of the invalid or unsupported syntax
 */
public data class EntitySelectorParseError(
    public val offset: Int,
    public val message: String,
)
