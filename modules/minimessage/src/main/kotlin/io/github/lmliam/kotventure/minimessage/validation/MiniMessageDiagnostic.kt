package io.github.lmliam.kotventure.minimessage.validation

import io.github.lmliam.kotventure.minimessage.validate
import net.kyori.adventure.text.minimessage.ParsingException

/**
 * One diagnostic from [validate].
 *
 * The hierarchy is sealed, so a `when` expression can handle every diagnostic type.
 */
public sealed interface MiniMessageDiagnostic {
    /**
     * Reports the first tag error from Adventure's strict parser.
     *
     * @property message Adventure's description. This value uses [ParsingException.detailMessage] when it is
     * available, and otherwise uses [ParsingException.message].
     * @property startIndex The start index in the original input, or [LOCATION_UNKNOWN] when Adventure did not report
     * it.
     * @property endIndex The end index in the original input, or [LOCATION_UNKNOWN] when Adventure did not report it.
     */
    public data class MalformedTag(
        public val message: String,
        public val startIndex: Int,
        public val endIndex: Int,
    ) : MiniMessageDiagnostic {
        /** Provides the sentinel for an unknown source position. */
        public companion object {
            /**
             * The value that [MalformedTag] uses when Adventure does not report a source position.
             */
            public val LOCATION_UNKNOWN: Int = ParsingException.LOCATION_UNKNOWN
        }
    }

    /**
     * Reports a declared placeholder that has no corresponding tag in the input.
     *
     * @property name The declared placeholder tag name.
     */
    public data class MissingPlaceholder(
        public val name: String,
    ) : MiniMessageDiagnostic

    /**
     * Reports a custom tag in the input that is not a declared placeholder.
     *
     * @property name The tag name from the input.
     */
    public data class ExtraPlaceholder(
        public val name: String,
    ) : MiniMessageDiagnostic

    /**
     * Reports an unexpected failure in a validation pass.
     *
     * @property message A description of the unexpected engine failure.
     */
    public data class ValidationEngineFailure(
        public val message: String,
    ) : MiniMessageDiagnostic
}
