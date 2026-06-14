package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.minimessage.validation.MiniMessageDiagnostic
import io.github.lmliam.kotventure.minimessage.validation.ValidationResult
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.Context
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * Validates [markup] against the declared [spec] placeholders.
 *
 * Two independent passes are run and their diagnostics are merged into a single list:
 *
 * - **Pass 1 (malformed tags):** The markup is parsed with `strict(true)` mode. Every spec name
 *   is resolved as a self-closing tag so spec-name placeholders do not produce false positives. A
 *   [ParsingException] is caught and emitted as [MiniMessageDiagnostic.MalformedTag]. Only the
 *   first exception per parse is reported — fixing it may reveal more.
 *
 * - **Pass 2 (placeholder presence):** A recording [TagResolver] records every non-standard tag
 *   name encountered during a lenient parse. Names in [spec] but absent from the markup produce
 *   [MiniMessageDiagnostic.MissingPlaceholder]; names in the markup but absent from [spec] produce
 *   [MiniMessageDiagnostic.ExtraPlaceholder].
 *
 * Diagnostic ordering in [ValidationResult.Failure.diagnostics]: malformed tags first, then
 * missing placeholders in spec declaration order, then extra placeholders in markup-encounter order.
 *
 * @param markup the MiniMessage markup string to validate.
 * @param spec the declared placeholders the markup is expected to use; order determines the order
 *   of [MiniMessageDiagnostic.MissingPlaceholder] entries in the result.
 * @return [ValidationResult.Success] when no issues were found, or
 *   [ValidationResult.Failure] with a non-empty list of diagnostics.
 */
public fun validate(
    markup: String,
    spec: Iterable<MiniMessagePlaceholder<*>>,
): ValidationResult {
    val specList = spec.toList()
    val malformed = detectMalformedTags(markup, specList)
    val (missing, extra) = detectPlaceholderMismatches(markup, specList)
    val diagnostics = malformed + missing + extra
    return if (diagnostics.isEmpty()) ValidationResult.Success else ValidationResult.Failure(diagnostics)
}

/**
 * Validates this template's markup against its own declared placeholders.
 *
 * Convenience wrapper over [validate] that extracts the spec from the template's
 * `@PublishedApi internal` [MiniTemplate.placeholders] map without exposing a new public surface.
 *
 * @return [ValidationResult.Success] when the markup is well-formed and every declared placeholder
 *   has a corresponding tag (and vice versa); [ValidationResult.Failure] otherwise.
 */
public fun MiniTemplate.validate(): ValidationResult = validate(markup, placeholders.values)

// ---------------------------------------------------------------------------
// Internal helpers
// ---------------------------------------------------------------------------

/**
 * Pass 1: strict-mode parse to detect malformed or unclosed tags.
 *
 * Spec names are resolved as self-closing tags so they never trigger a false "unclosed tag"
 * diagnostic under strict mode. Returns at most one [MiniMessageDiagnostic.MalformedTag]
 * because Adventure's strict parser throws on the first violation.
 *
 * [ParsingException] is the expected signal for strict-mode violations. Other [RuntimeException]
 * subclasses can be thrown by Adventure's parser internals on severely malformed inputs; they
 * are caught to preserve the no-throw contract of [validate].
 */
private fun detectMalformedTags(
    markup: String,
    spec: List<MiniMessagePlaceholder<*>>,
): List<MiniMessageDiagnostic.MalformedTag> {
    val strictParser = MiniMessage.builder().strict(true).build()
    val specResolver = buildSpecNameResolver(spec)
    val combined = TagResolver.resolver(TagResolver.standard(), specResolver)
    return try {
        strictParser.deserialize(markup, combined)
        emptyList()
    } catch (e: ParsingException) {
        listOf(
            MiniMessageDiagnostic.MalformedTag(
                message = e.detailMessage() ?: e.message ?: "",
                startIndex = e.startIndex(),
                endIndex = e.endIndex(),
            ),
        )
    } catch (_: RuntimeException) {
        // Adventure parser bug on edge-case malformed input — no position info available.
        emptyList()
    }
}

/**
 * Pass 2: lenient parse with a recording resolver to enumerate tags referenced in the markup.
 *
 * Records a tag name when it is either non-standard (i.e. [TagResolver.standard] does not
 * claim it) **or** present in the spec. The second condition handles the edge case where a
 * spec placeholder shares its name with a standard Adventure tag (e.g. `"gold"`): without it,
 * the recording resolver would silently skip `<gold>` and produce a false `MissingPlaceholder`.
 *
 * Standard tags whose names are NOT in the spec are still excluded, preserving the existing
 * behaviour that prevents ordinary formatting tags from showing up as extra placeholders.
 *
 * Returns a pair of (missing diagnostics, extra diagnostics):
 * - missing: spec names absent from the recorded tags (in spec declaration order).
 * - extra: recorded names absent from the spec (in markup-encounter order).
 */
private fun detectPlaceholderMismatches(
    markup: String,
    spec: List<MiniMessagePlaceholder<*>>,
): Pair<List<MiniMessageDiagnostic.MissingPlaceholder>, List<MiniMessageDiagnostic.ExtraPlaceholder>> {
    val specNames = spec.map { it.name }.toSet()
    val recorder = RecordingTagResolver(specNames)
    val combined = TagResolver.resolver(TagResolver.standard(), recorder)
    // The lenient parser is designed not to throw ParsingException, but Adventure's internals
    // can still throw RuntimeException (e.g. StringIndexOutOfBoundsException) on certain
    // edge-case malformed inputs (PaperMC/adventure#1011). Catch it so validate() never
    // propagates an exception; the recording side-effects gathered so far remain usable.
    try {
        MiniMessage.miniMessage().deserialize(markup, combined)
    } catch (_: RuntimeException) {
        // Proceed with whatever the recorder captured before the throw.
    }

    val tagsInMarkup = recorder.encounteredNames

    val missing =
        spec
        .filter { it.name !in tagsInMarkup }
        .map { MiniMessageDiagnostic.MissingPlaceholder(it.name) }

    val extra =
        tagsInMarkup
        .filter { it !in specNames }
        .map { MiniMessageDiagnostic.ExtraPlaceholder(it) }

    return missing to extra
}

/**
 * Builds a [TagResolver] that resolves each spec name as a self-closing inserting tag.
 *
 * Self-closing tags are never required to be explicitly closed, so they do not produce
 * false-positive "unclosed tag" diagnostics under strict mode.
 */
private fun buildSpecNameResolver(spec: List<MiniMessagePlaceholder<*>>): TagResolver {
    if (spec.isEmpty()) return TagResolver.empty()
    val selfClosingTag = Tag.selfClosingInserting(Component.empty())
    val resolvers = spec.map { TagResolver.resolver(it.name, selfClosingTag) }
    return TagResolver.resolver(*resolvers.toTypedArray())
}

/**
 * A [TagResolver] that records the names of tags it is asked to resolve.
 *
 * [has] returns `true` for all names so that Adventure calls [resolve] for every `<tag>` in
 * the markup. [resolve] records the name when either:
 * - [TagResolver.standard] does not claim it (it's a custom/unknown tag), or
 * - it appears in [specNames] (a spec placeholder whose name collides with a standard tag).
 *
 * This dual condition prevents standard formatting tags (e.g. `<red>`, `<bold>`) from polluting
 * the recorded set, while still recording spec-named placeholders that share a standard tag
 * name (e.g. a placeholder declared as `"gold"`).
 *
 * [resolve] returns `null` so the lenient parser silently skips each tag.
 *
 * Tags are recorded in encounter order (the order [resolve] is first called for each name).
 *
 * @param specNames the set of placeholder names declared in the spec; used to force-record
 *   names that would otherwise be filtered by the standard-tag check.
 */
private class RecordingTagResolver(
    private val specNames: Set<String>,
) : TagResolver {
    /** Tag names encountered in the markup, in the order they were first seen. */
    val encounteredNames: LinkedHashSet<String> = LinkedHashSet()

    override fun resolve(
        name: String,
        arguments: ArgumentQueue,
        ctx: Context,
    ): Tag? {
        if (name in specNames || !TagResolver.standard().has(name)) {
            encounteredNames.add(name)
        }
        return null
    }

    override fun has(name: String): Boolean = true
}
