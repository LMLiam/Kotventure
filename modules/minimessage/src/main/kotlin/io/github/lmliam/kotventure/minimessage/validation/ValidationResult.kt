package io.github.lmliam.kotventure.minimessage.validation

/**
 * The result of validating MiniMessage markup and its declared placeholders.
 *
 * [Success] indicates that strict parsing succeeded and that the markup and declarations contain the same placeholder
 * names. [Failure] contains one or more diagnostics.
 */
public sealed interface ValidationResult {
    /** Whether this result is [Success]. */
    public val isSuccess: Boolean
        get() = this is Success

    /** Whether this result is [Failure]. */
    public val isFailure: Boolean
        get() = this is Failure

    /** Indicates that validation found no issues. */
    public data object Success : ValidationResult

    /**
     * Contains the issues found during validation.
     *
     * Diagnostics are ordered as follows:
     *
     * 1. malformed-tag or strict-engine diagnostic;
     * 2. missing placeholders in declaration order;
     * 3. extra placeholders in input encounter order.
     *
     * @property diagnostics a non-empty immutable snapshot of the diagnostics.
     */
    @ConsistentCopyVisibility
    public data class Failure private constructor(
        public val diagnostics: List<MiniMessageDiagnostic>,
    ) : ValidationResult {
        init {
            require(diagnostics.isNotEmpty()) {
                "Validation failure must contain at least one diagnostic."
            }
        }

        public companion object {
            /**
             * Creates a validation failure from [diagnostics].
             *
             * @throws IllegalArgumentException when [diagnostics] is empty.
             */
            public operator fun invoke(diagnostics: List<MiniMessageDiagnostic>): Failure =
                Failure(diagnostics.toList())
        }
    }
}

internal fun List<MiniMessageDiagnostic>.toValidationResult(): ValidationResult =
    if (isEmpty()) {
        ValidationResult.Success
    } else {
        ValidationResult.Failure(this)
    }
