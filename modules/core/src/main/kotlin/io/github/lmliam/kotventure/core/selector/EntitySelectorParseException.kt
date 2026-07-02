package io.github.lmliam.kotventure.core.selector

/**
 * Reports the first failure found while parsing an entity selector with [entitySelector].
 *
 * @property offset zero-based index into the parsed source string at which the failure was detected
 * @property message actionable explanation of the invalid or unsupported syntax
 */
public class EntitySelectorParseException internal constructor(
    public val offset: Int,
    override val message: String,
) : IllegalArgumentException(message)
