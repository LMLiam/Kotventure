package io.github.lmliam.kotventure.minimessage.validation

/**
 * The result of validating MiniMessage markup and its declared placeholders.
 *
 * [Success] means that strict parsing succeeded and the markup and declarations contain the same placeholder names.
 * [Failure] contains one or more diagnostics.
 */
public sealed interface ValidationResult {
    /** `true` when this result is [Success]. Otherwise, `false`. */
    public val isSuccess: Boolean get() = this is Success

    /** `true` when this result is [Failure]. Otherwise, `false`. */
    public val isFailure: Boolean get() = this is Failure

    /**
     * Indicates that validation found no tag or placeholder issues.
     */
    public data object Success : ValidationResult

    /**
     * Contains the issues that validation found.
     *
     * @property diagnostics A non-empty immutable snapshot. A malformed-tag diagnostic comes first. Missing-placeholder
     * diagnostics then follow declaration order. Extra-placeholder diagnostics follow their first occurrence in the
     * input. A [MiniMessageDiagnostic.ValidationEngineFailure] replaces the diagnostics of a pass that failed
     * unexpectedly.
     */
    @ConsistentCopyVisibility
    public data class Failure private constructor(
        public val diagnostics: List<MiniMessageDiagnostic>,
    ) : ValidationResult {
        init {
            require(diagnostics.isNotEmpty()) { "Failure must carry at least one diagnostic." }
        }

        /** Creates [Failure] values. */
        public companion object {
            /**
             * Creates a failure from a snapshot of [diagnostics].
             *
             * @throws IllegalArgumentException when [diagnostics] is empty.
             */
            public operator fun invoke(diagnostics: List<MiniMessageDiagnostic>): Failure =
                Failure(diagnostics.toList())
        }
    }
}
