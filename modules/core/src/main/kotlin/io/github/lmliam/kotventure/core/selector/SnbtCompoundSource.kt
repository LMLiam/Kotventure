package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.selector.parsing.SelectorReader
import io.github.lmliam.kotventure.core.selector.parsing.validateSnbtCompound

/**
 * The source text of one validated SNBT compound.
 *
 * Use [parse] to create a value from external text. The value keeps the original spelling and whitespace. It is
 * immutable.
 *
 * @property value The validated source, including the opening and closing braces.
 */
@JvmInline
public value class SnbtCompoundSource internal constructor(
    public val value: String,
) {
    /** Creates validated compound SNBT source. */
    public companion object {
        /**
         * Validates and returns [source] as one complete SNBT compound.
         *
         * @throws EntitySelectorParseException when [source] is not one complete compound. The exception offset is
         * the position at which validation failed.
         */
        public fun parse(source: String): SnbtCompoundSource =
            SelectorReader(source).run {
                validateSnbtCompound()
                if (!isAtEnd()) fail("Unexpected trailing SNBT content")
                SnbtCompoundSource(source)
            }
    }

    /** Returns the original SNBT source text. */
    public override fun toString(): String = value
}
