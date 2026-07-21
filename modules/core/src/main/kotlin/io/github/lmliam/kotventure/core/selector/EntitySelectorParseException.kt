package io.github.lmliam.kotventure.core.selector

/**
 * Reports the first failure that [parseSelector] finds.
 *
 * @property offset The zero-based index in the source at which the parser detected the failure.
 * @property message An explanation of the invalid or unsupported syntax.
 */
public class EntitySelectorParseException internal constructor(
    public val offset: Int,
    override val message: String,
    cause: Throwable? = null,
) : IllegalArgumentException(message, cause)
