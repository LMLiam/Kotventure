package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import io.github.lmliam.kotventure.minimessage.template.MiniTemplate
import io.github.lmliam.kotventure.minimessage.validation.ValidationResult
import io.github.lmliam.kotventure.minimessage.validation.runValidation

/**
 * Validates [input] against the declared [placeholders].
 *
 * Diagnostics report malformed tags, declared placeholders missing from the markup, and custom tags
 * in the markup that were not declared.
 */
public fun validate(
    input: String,
    placeholders: Iterable<MiniMessagePlaceholder<*>>,
): ValidationResult = runValidation(input, placeholders.toList())

/** Validates this template's markup against its declared placeholders. */
public fun MiniTemplate.validate(): ValidationResult = validation
