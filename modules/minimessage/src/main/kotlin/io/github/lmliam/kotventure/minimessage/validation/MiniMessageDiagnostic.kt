package io.github.lmliam.kotventure.minimessage.validation

import net.kyori.adventure.text.minimessage.ParsingException

/**
 * A single diagnostic produced by [io.github.lmliam.kotventure.minimessage.validate].
 *
 * Sealed so callers can exhaustively handle every diagnostic kind.
 */
public sealed class MiniMessageDiagnostic {
    /**
     * A tag in the markup is malformed or was not explicitly closed when strict parsing requires it.
     *
     * @property message Human-readable description from Adventure's parser. Prefers
     *   [ParsingException.detailMessage] (no location noise); falls back to
     *   [ParsingException.getMessage] if `detailMessage` is null.
     * @property startIndex Start index into the original markup string, or [LOCATION_UNKNOWN] when
     *   Adventure did not report a source position.
     * @property endIndex End index into the original markup string, or [LOCATION_UNKNOWN] when
     *   Adventure did not report a source position.
     */
    public data class MalformedTag(
        public val message: String,
        public val startIndex: Int,
        public val endIndex: Int,
    ) : MiniMessageDiagnostic() {
        public companion object {
            /**
             * Sentinel used when Adventure did not report a source position for the malformed tag.
             *
             * Mirrors [ParsingException.LOCATION_UNKNOWN] directly so the value is always
             * Adventure-derived. Do not hardcode `-1`; read this constant instead.
             */
            public val LOCATION_UNKNOWN: Int = ParsingException.LOCATION_UNKNOWN
        }
    }

    /**
     * A placeholder declared in the spec has no corresponding tag in the markup.
     *
     * @property name The placeholder's tag name as declared in the spec.
     */
    public data class MissingPlaceholder(
        public val name: String,
    ) : MiniMessageDiagnostic()

    /**
     * A placeholder tag appears in the markup but has no entry in the spec.
     *
     * @property name The tag name found in the markup.
     */
    public data class ExtraPlaceholder(
        public val name: String,
    ) : MiniMessageDiagnostic()
}
