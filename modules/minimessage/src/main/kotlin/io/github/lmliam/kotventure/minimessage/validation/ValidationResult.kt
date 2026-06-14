package io.github.lmliam.kotventure.minimessage.validation

/**
 * The result of a [io.github.lmliam.kotventure.minimessage.validate] call.
 *
 * Either [Success] (markup is well-formed and every spec placeholder is present) or
 * [Failure] (one or more [MiniMessageDiagnostic] were found).
 */
public sealed class ValidationResult {
    /**
     * Validation found no issues: the markup is well-formed and every declared placeholder
     * has a corresponding tag in the markup (and vice versa).
     */
    public data object Success : ValidationResult()

    /**
     * Validation found one or more issues.
     *
     * @property diagnostics Non-empty list of diagnostics. Ordering guarantee: malformed-tag
     *   diagnostics appear first, then missing-placeholder diagnostics in spec declaration order,
     *   then extra-placeholder diagnostics in the order the tags were encountered in the markup.
     */
    public data class Failure(
        public val diagnostics: List<MiniMessageDiagnostic>,
    ) : ValidationResult() {
        init {
            require(diagnostics.isNotEmpty()) { "Failure must carry at least one diagnostic." }
        }
    }
}

/**
 * `true` when this result is [ValidationResult.Success]; `false` otherwise.
 */
public val ValidationResult.isSuccess: Boolean
    get() = this is ValidationResult.Success

/**
 * `true` when this result is [ValidationResult.Failure]; `false` otherwise.
 */
public val ValidationResult.isFailure: Boolean
    get() = this is ValidationResult.Failure
