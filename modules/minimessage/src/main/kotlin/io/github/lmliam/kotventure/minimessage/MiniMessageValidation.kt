package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import io.github.lmliam.kotventure.minimessage.template.MiniTemplate
import io.github.lmliam.kotventure.minimessage.validation.ValidationResult
import io.github.lmliam.kotventure.minimessage.validation.runValidation

/**
 * Validates [input] against the declared [placeholders].
 *
 * Validation uses Adventure's strict parser to detect malformed or unclosed tags. A separate lenient pass compares
 * declared placeholder names with custom tags found in [input].
 *
 * The placeholder collection is snapshotted before validation. Missing-placeholder diagnostics follow declaration
 * order, while extra-placeholder diagnostics follow their first occurrence in [input].
 */
public fun validate(
    input: String,
    placeholders: Iterable<MiniMessagePlaceholder<*>>,
): ValidationResult =
    runValidation(
        input = input,
        placeholders = placeholders.toList(),
    )

/**
 * Returns the cached validation result for this template.
 *
 * The first caller performs validation. Concurrent callers wait for that computation and receive the same cached
 * result.
 */
public fun MiniTemplate.validate(): ValidationResult = validation
