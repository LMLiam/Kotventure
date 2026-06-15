package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.minimessage.validation.ValidationResult
import io.github.lmliam.kotventure.minimessage.validation.runValidation

/**
 * Validates [input] against the declared [placeholders].
 *
 * Two independent passes are run and their diagnostics are merged into a single list:
 *
 * - **Pass 1 (malformed tags):** The input is parsed with `strict(true)` mode. Every placeholder
 *   name is resolved as a self-closing tag so placeholder-name tags do not produce false positives.
 *   A [net.kyori.adventure.text.minimessage.ParsingException] is caught and emitted as a
 *   [io.github.lmliam.kotventure.minimessage.validation.MiniMessageDiagnostic.MalformedTag]. Only
 *   the first exception per parse is reported — fixing it may reveal more.
 *
 * - **Pass 2 (placeholder presence):** A recording
 *   [net.kyori.adventure.text.minimessage.tag.resolver.TagResolver] records every non-standard tag
 *   name encountered during a lenient parse. Names in [placeholders] but absent from the input
 *   produce [io.github.lmliam.kotventure.minimessage.validation.MiniMessageDiagnostic.MissingPlaceholder];
 *   names in the input but absent from [placeholders] produce
 *   [io.github.lmliam.kotventure.minimessage.validation.MiniMessageDiagnostic.ExtraPlaceholder].
 *
 * Diagnostic ordering in [ValidationResult.Failure.diagnostics]: malformed tags first, then
 * missing placeholders in [placeholders] declaration order, then extra placeholders in
 * input-encounter order.
 *
 * @param input the MiniMessage markup string to validate.
 * @param placeholders the declared placeholders the input is expected to use; order determines the
 *   order of [io.github.lmliam.kotventure.minimessage.validation.MiniMessageDiagnostic.MissingPlaceholder]
 *   entries in the result.
 * @return [ValidationResult.Success] when no issues were found, or [ValidationResult.Failure] with
 *   a non-empty list of diagnostics.
 */
public fun validate(
    input: String,
    placeholders: Iterable<MiniMessagePlaceholder<*>>,
): ValidationResult = runValidation(input, placeholders.toList())

/**
 * Validates this template's markup against its own declared placeholders.
 *
 * Convenience wrapper over [validate] that extracts the placeholder list from the template's
 * `@PublishedApi internal` [MiniTemplate.placeholders] map without exposing a new public surface.
 *
 * @return [ValidationResult.Success] when the markup is well-formed and every declared placeholder
 *   has a corresponding tag (and vice versa); [ValidationResult.Failure] otherwise.
 */
public fun MiniTemplate.validate(): ValidationResult = validate(markup, placeholders.values)
