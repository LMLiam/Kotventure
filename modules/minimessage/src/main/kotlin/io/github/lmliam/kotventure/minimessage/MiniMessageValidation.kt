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
    }
}

/**
 * Pass 2: lenient parse with a recording resolver to enumerate tags referenced in the markup.
 *
 * Records every non-standard tag name (i.e. names for which [TagResolver.standard] returns
 * `false` from [TagResolver.has]) encountered by the recording resolver. Non-standard names
 * are exactly the placeholder-like tags; standard Adventure formatting tags are excluded.
 *
 * Returns a pair of (missing diagnostics, extra diagnostics):
 * - missing: spec names absent from the recorded tags (in spec declaration order).
 * - extra: recorded names absent from the spec (in markup-encounter order).
 */
private fun detectPlaceholderMismatches(
    markup: String,
    spec: List<MiniMessagePlaceholder<*>>,
): Pair<List<MiniMessageDiagnostic.MissingPlaceholder>, List<MiniMessageDiagnostic.ExtraPlaceholder>> {
    val recorder = RecordingTagResolver()
    val combined = TagResolver.resolver(TagResolver.standard(), recorder)
    MiniMessage.miniMessage().deserialize(markup, combined)

    val tagsInMarkup = recorder.encounteredNames
    val specNames = spec.map { it.name }.toSet()

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
 * A [TagResolver] that records the names of every non-standard tag it is asked to resolve.
 *
 * [has] returns `true` for all names so that Adventure calls [resolve] for every `<tag>` in
 * the markup. [resolve] records the name only when [TagResolver.standard] does not claim it,
 * preventing standard Adventure formatting tags from appearing in the recorded set.
 * [resolve] returns `null` so the lenient parser silently skips each tag.
 *
 * Tags are recorded in encounter order (the order [resolve] is first called for each name).
 */
private class RecordingTagResolver : TagResolver {
    /** Tag names encountered in the markup, in the order they were first seen. */
    val encounteredNames: LinkedHashSet<String> = LinkedHashSet()

    override fun resolve(
        name: String,
        arguments: ArgumentQueue,
        ctx: Context,
    ): Tag? {
        if (!TagResolver.standard().has(name)) {
            encounteredNames.add(name)
        }
        return null
    }

    override fun has(name: String): Boolean = true
}
