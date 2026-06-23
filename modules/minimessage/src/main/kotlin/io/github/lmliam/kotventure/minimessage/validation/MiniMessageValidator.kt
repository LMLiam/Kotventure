package io.github.lmliam.kotventure.minimessage.validation

import io.github.lmliam.kotventure.minimessage.MiniMessagePlaceholder
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

private val STRICT_MINI_MESSAGE: MiniMessage = MiniMessage.builder().strict(true).build()
private val LENIENT_MINI_MESSAGE: MiniMessage = MiniMessage.miniMessage()

/**
 * Runs the two-pass validation algorithm and returns the merged [ValidationResult].
 *
 * - **Pass 1** detects malformed or unclosed tags via a strict-mode parse.
 * - **Pass 2** detects placeholder mismatches via a recording lenient parse.
 *
 * Diagnostic ordering: malformed tags first, then missing placeholders in [placeholders]
 * declaration order, then extra placeholders in input-encounter order.
 *
 * [ParsingException] is the documented signal for strict-mode violations and is reported as a
 * malformed-tag diagnostic. The lenient parser is not contractually guaranteed exception-free for
 * every input, so any other [RuntimeException] is turned into a diagnostic rather than propagated —
 * `validate` never throws for the caller.
 *
 * @param input the MiniMessage input string to validate.
 * @param placeholders the declared placeholders the input is expected to use.
 * @return [ValidationResult.Success] when no issues were found, or [ValidationResult.Failure]
 *   with a non-empty list of diagnostics.
 */
internal fun runValidation(
    input: String,
    placeholders: List<MiniMessagePlaceholder<*>>,
): ValidationResult {
    val malformed = detectMalformedTags(input, placeholders)
    val (missing, extra) = detectPlaceholderMismatches(input, placeholders)
    val diagnostics = malformed + missing + extra
    return if (diagnostics.isEmpty()) ValidationResult.Success else ValidationResult.Failure(diagnostics)
}

/**
 * Pass 1: strict-mode parse to detect malformed or unclosed tags.
 *
 * Placeholder names are resolved as self-closing tags so they never trigger a false
 * "unclosed tag" diagnostic under strict mode. Returns at most one
 * [MiniMessageDiagnostic.MalformedTag] because Adventure's strict parser throws on the first
 * violation.
 *
 * [ParsingException] is the documented signal for strict-mode violations. Any other
 * [RuntimeException] from the parser is also reported as a malformed-tag diagnostic (with an unknown
 * position) so a crash-inducing input is never silently treated as valid and `validate` never throws.
 */
private fun detectMalformedTags(
    input: String,
    placeholders: List<MiniMessagePlaceholder<*>>,
): List<MiniMessageDiagnostic> {
    val placeholderResolver = buildPlaceholderNameResolver(placeholders)
    val combined = TagResolver.resolver(TagResolver.standard(), placeholderResolver)
    return try {
        STRICT_MINI_MESSAGE.deserialize(input, combined)
        emptyList()
    } catch (e: ParsingException) {
        listOf(
            MiniMessageDiagnostic.MalformedTag(
                message = e.detailMessage() ?: e.message ?: "",
                startIndex = e.startIndex(),
                endIndex = e.endIndex(),
            ),
        )
    } catch (e: RuntimeException) {
        listOf(MiniMessageDiagnostic.ValidationEngineFailure(e.message ?: "MiniMessage validation failed."))
    }
}

/**
 * Pass 2: lenient parse with a recording resolver to enumerate tags referenced in the input.
 *
 * Records a tag name when it is either non-standard (i.e. [TagResolver.standard] does not
 * claim it) **or** present in the placeholders. The second condition handles the edge case where
 * a placeholder shares its name with a standard Adventure tag (e.g. `"gold"`): without it, the
 * recording resolver would silently skip `<gold>` and produce a false `MissingPlaceholder`.
 *
 * Standard tags whose names are NOT in the placeholders are still excluded, preserving the
 * behaviour that prevents ordinary formatting tags from showing up as extra placeholders.
 *
 * Returns a pair of (missing diagnostics, extra diagnostics):
 * - missing: placeholder names absent from the recorded tags (in declaration order).
 * - extra: recorded names absent from the placeholders (in input-encounter order).
 */
private fun detectPlaceholderMismatches(
    input: String,
    placeholders: List<MiniMessagePlaceholder<*>>,
): Pair<List<MiniMessageDiagnostic.MissingPlaceholder>, List<MiniMessageDiagnostic.ExtraPlaceholder>> {
    val specNames = placeholders.map { it.name }.toSet()
    val recorder = RecordingTagResolver(specNames)
    val combined = TagResolver.resolver(TagResolver.standard(), recorder)
    // The lenient parser is not contractually guaranteed exception-free for every input. Guard so
    // validate() never propagates an exception; any input that crashes the parser is already
    // reported as malformed by the strict pass, and the recorder's side-effects so far remain usable.
    try {
        LENIENT_MINI_MESSAGE.deserialize(input, combined)
    } catch (_: RuntimeException) {
        // Proceed with whatever the recorder captured before the throw.
    }

    val tagsInInput = recorder.encounteredNames

    val missing =
        placeholders
            .filter { it.name !in tagsInInput }
            .map { MiniMessageDiagnostic.MissingPlaceholder(it.name) }

    val extra =
        tagsInInput
            .filter { it !in specNames }
            .map { MiniMessageDiagnostic.ExtraPlaceholder(it) }

    return missing to extra
}

/**
 * Builds a [TagResolver] that resolves each placeholder name as a self-closing inserting tag.
 *
 * Self-closing tags are never required to be explicitly closed, so they do not produce
 * false-positive "unclosed tag" diagnostics under strict mode.
 */
private fun buildPlaceholderNameResolver(placeholders: List<MiniMessagePlaceholder<*>>): TagResolver {
    if (placeholders.isEmpty()) return TagResolver.empty()
    val selfClosingTag = Tag.selfClosingInserting(Component.empty())
    val resolvers = placeholders.map { TagResolver.resolver(it.name, selfClosingTag) }
    return TagResolver.resolver(*resolvers.toTypedArray())
}
