package io.github.lmliam.kotventure.minimessage.validation

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder

/**
 * Runs all MiniMessage validation passes for [input].
 *
 * Strict-parser diagnostics are emitted first. Missing placeholders then follow declaration order, and extra
 * placeholders follow their first occurrence in [input].
 */
internal fun runValidation(
    input: String,
    placeholders: List<MiniMessagePlaceholder<*>>,
): ValidationResult =
    buildList {
        validateStrictMarkup(
            input = input,
            placeholders = placeholders,
        )?.let { add(it) }

        addAll(
            validatePlaceholderUsage(
                input = input,
                placeholders = placeholders,
            ),
        )
    }.toValidationResult()
