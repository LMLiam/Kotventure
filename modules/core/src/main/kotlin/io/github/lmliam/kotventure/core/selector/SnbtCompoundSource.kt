package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.selector.parsing.SelectorReader
import io.github.lmliam.kotventure.core.selector.parsing.validateSnbtCompound

/**
 * Validated compound SNBT source.
 *
 * Construct with [parse].
 *
 * @property value validated source beginning with `{` and ending with `}`
 */
@JvmInline
public value class SnbtCompoundSource private constructor(
    public val value: String,
) {
    /** Validated compound SNBT construction. */
    public companion object {
        /**
         * Validates [source] as one complete SNBT compound.
         *
         * @throws EntitySelectorParseException if [source] is not a compound or has trailing content
         */
        public fun parse(source: String): SnbtCompoundSource {
            val reader = SelectorReader(source)
            reader.validateSnbtCompound()
            if (!reader.isAtEnd()) reader.fail("Unexpected trailing SNBT content")
            return SnbtCompoundSource(source)
        }

        /**
         * Wraps [source] after a caller has already validated or rendered the complete compound.
         */
        internal fun trusted(source: String): SnbtCompoundSource = SnbtCompoundSource(source)
    }

    public override fun toString(): String = value
}
