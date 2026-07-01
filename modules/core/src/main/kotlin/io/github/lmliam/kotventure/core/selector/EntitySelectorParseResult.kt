package io.github.lmliam.kotventure.core.selector

/**
 * Result of opt-in entity-selector parsing.
 */
public sealed interface EntitySelectorParseResult {
    /** A successfully parsed selector. */
    public data class Success(
        public val selector: ParsedEntitySelector,
    ) : EntitySelectorParseResult

    /** A selector that could not be parsed without loss. */
    public data class Failure(
        public val error: EntitySelectorParseError,
    ) : EntitySelectorParseResult
}

/**
 * A precise entity-selector parse failure.
 *
 * [offset] is the zero-based Kotlin string index at which parsing failed.
 */
public data class EntitySelectorParseError(
    public val offset: Int,
    public val message: String,
)
