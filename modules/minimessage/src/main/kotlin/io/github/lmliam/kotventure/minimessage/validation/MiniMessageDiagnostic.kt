package io.github.lmliam.kotventure.minimessage.validation

import io.github.lmliam.kotventure.minimessage.validate
import net.kyori.adventure.text.minimessage.ParsingException

/**
 * One diagnostic produced by [validate].
 *
 * The hierarchy is sealed so callers can handle every diagnostic type exhaustively.
 */
public sealed interface MiniMessageDiagnostic {
    /**
     * Reports the first malformed or unclosed tag found by Adventure's strict parser.
     *
     * @property message Adventure's diagnostic description.
     * @property startIndex the start index in the original input, or [LOCATION_UNKNOWN] when unavailable.
     * @property endIndex the end index in the original input, or [LOCATION_UNKNOWN] when unavailable.
     */
    public data class MalformedTag(
        public val message: String,
        public val startIndex: Int,
        public val endIndex: Int,
    ) : MiniMessageDiagnostic {
        /** Constants used by malformed-tag diagnostics. */
        public companion object {
            /**
             * The value used when Adventure does not provide a source position.
             */
            public const val LOCATION_UNKNOWN: Int =
                ParsingException.LOCATION_UNKNOWN
        }
    }

    /**
     * Reports a declared placeholder that does not occur in the input.
     *
     * @property name the declared placeholder name.
     */
    public data class MissingPlaceholder(
        public val name: String,
    ) : MiniMessageDiagnostic

    /**
     * Reports a custom tag in the input that was not declared as a placeholder.
     *
     * @property name the tag name found in the input.
     */
    public data class ExtraPlaceholder(
        public val name: String,
    ) : MiniMessageDiagnostic

    /**
     * Reports an unexpected failure inside a validation pass.
     *
     * @property message a description of the unexpected failure.
     */
    public data class ValidationEngineFailure(
        public val message: String,
    ) : MiniMessageDiagnostic
}

internal fun ParsingException.toMalformedTagDiagnostic(): MiniMessageDiagnostic.MalformedTag =
    MiniMessageDiagnostic.MalformedTag(
        message =
            detailMessage().orFallback(
                message.orFallback("MiniMessage parsing failed."),
            ),
        startIndex = startIndex(),
        endIndex = endIndex(),
    )

internal fun RuntimeException.toValidationEngineFailure(
    fallbackMessage: String,
): MiniMessageDiagnostic.ValidationEngineFailure =
    MiniMessageDiagnostic.ValidationEngineFailure(
        message = message.orFallback(fallbackMessage),
    )

private fun String?.orFallback(fallback: String): String = takeUnless { it.isNullOrBlank() } ?: fallback
