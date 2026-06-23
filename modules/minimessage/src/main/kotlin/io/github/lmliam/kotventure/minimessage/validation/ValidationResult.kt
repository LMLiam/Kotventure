package io.github.lmliam.kotventure.minimessage.validation

/**
 * The outcome of validating MiniMessage markup against its declared placeholders.
 *
 * Either [Success] (the markup is well-formed and every declared placeholder is present) or
 * [Failure] (one or more [MiniMessageDiagnostic] were found).
 */
public sealed interface ValidationResult {
    /** `true` when this result is [Success]; `false` otherwise. */
    public val isSuccess: Boolean get() = this is Success

    /** `true` when this result is [Failure]; `false` otherwise. */
    public val isFailure: Boolean get() = this is Failure

    /**
     * Validation found no issues: the input is well-formed and every declared placeholder
     * has a corresponding tag in the input (and vice versa).
     */
    public data object Success : ValidationResult

    /**
     * Validation found one or more issues.
     *
     * @property diagnostics Non-empty list of diagnostics. Ordering guarantee: malformed-tag
     *   diagnostics appear first, then missing-placeholder diagnostics in placeholder declaration
     *   order, then extra-placeholder diagnostics in the order the tags were encountered in the
     *   input.
     */
    @ConsistentCopyVisibility
    public data class Failure private constructor(
        public val diagnostics: List<MiniMessageDiagnostic>,
    ) : ValidationResult {
        init {
            require(diagnostics.isNotEmpty()) { "Failure must carry at least one diagnostic." }
        }

        public companion object {
            /** Creates a [Failure] with a defensive copy of [diagnostics]. */
            public operator fun invoke(diagnostics: List<MiniMessageDiagnostic>): Failure =
                Failure(diagnostics.toList())
        }
    }
}
