package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import io.github.lmliam.kotventure.minimessage.template.MiniTemplate
import io.github.lmliam.kotventure.minimessage.validation.ValidationResult
import io.github.lmliam.kotventure.minimessage.validation.runValidation

/**
 * Validates [input] against the declared [placeholders].
 *
 * Validation uses Adventure's strict parser to find a malformed or unclosed tag. A separate lenient pass compares
 * declared placeholder names with custom tags in the input. The function returns diagnostics and does not throw for a
 * normal validation failure.
 *
 * The function takes a snapshot of [placeholders]. Missing-placeholder diagnostics follow declaration order. Extra
 * placeholders follow their first occurrence in [input].
 */
public fun validate(
    input: String,
    placeholders: Iterable<MiniMessagePlaceholder<*>>,
): ValidationResult = runValidation(input, placeholders.toList())

/**
 * Returns the cached validation result for this template's markup and declared placeholders.
 *
 * The first call performs validation. Concurrent calls can perform the first validation more than one time, but all
 * successful computations have the same result.
 */
public fun MiniTemplate.validate(): ValidationResult = validation
