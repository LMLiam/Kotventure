package io.github.lmliam.kotventure.core.selector

import io.github.lmliam.kotventure.core.selector.parsing.SelectorReader
import io.github.lmliam.kotventure.core.selector.parsing.validateSnbtCompound

/**
 * Validated compound SNBT source.
 *
 * Construct with [parse]. The direct constructor is module-internal: it wraps source a caller has
 * already validated or rendered as one complete compound.
 *
 * @property value validated source beginning with `{` and ending with `}`
 */
@JvmInline
public value class SnbtCompoundSource internal constructor(
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
    }

    public override fun toString(): String = value
}
