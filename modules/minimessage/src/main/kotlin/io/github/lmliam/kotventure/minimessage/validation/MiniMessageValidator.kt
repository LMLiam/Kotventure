package io.github.lmliam.kotventure.minimessage.validation

import io.github.lmliam.kotventure.core.component.emptyComponent
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

private val STRICT_MINI_MESSAGE: MiniMessage = MiniMessage.builder().strict(true).build()
private val LENIENT_MINI_MESSAGE: MiniMessage = MiniMessage.miniMessage()

/**
 * Runs strict malformed-tag detection and lenient placeholder detection for [input].
 *
 * Diagnostics are emitted in this order: malformed tags first, then missing and extra placeholders.
 * If the lenient pass fails unexpectedly, it contributes a single
 * [MiniMessageDiagnostic.ValidationEngineFailure] instead of partial mismatch diagnostics.
 */
internal fun runValidation(
    input: String,
    placeholders: List<MiniMessagePlaceholder<*>>,
): ValidationResult {
    val malformed = detectMalformedTags(input, placeholders)
    val mismatches = detectPlaceholderMismatches(input, placeholders)
    val diagnostics = malformed + mismatches
    return if (diagnostics.isEmpty()) ValidationResult.Success else ValidationResult.Failure(diagnostics)
}

/** Uses Adventure strict mode to detect malformed or unclosed tags. */
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
 * Uses a recording lenient parse to detect missing and extra placeholders.
 *
 * Placeholder names that collide with standard Adventure tags are still recorded when declared, so
 * `<gold>` can satisfy a placeholder named `gold`.
 */
private fun detectPlaceholderMismatches(
    input: String,
    placeholders: List<MiniMessagePlaceholder<*>>,
): List<MiniMessageDiagnostic> {
    val specNames = placeholders.map { it.name }.toSet()
    val recorder = RecordingTagResolver(specNames)
    val combined = TagResolver.resolver(TagResolver.standard(), recorder)
    return try {
        LENIENT_MINI_MESSAGE.deserialize(input, combined)
        buildPlaceholderMismatchDiagnostics(placeholders, specNames, recorder.encounteredNames)
    } catch (e: RuntimeException) {
        listOf(MiniMessageDiagnostic.ValidationEngineFailure(e.message ?: "MiniMessage validation failed."))
    }
}

private fun buildPlaceholderMismatchDiagnostics(
    placeholders: List<MiniMessagePlaceholder<*>>,
    specNames: Set<String>,
    tagsInInput: Collection<String>,
): List<MiniMessageDiagnostic> {
    val missing =
        placeholders
            .filter { it.name !in tagsInInput }
            .map { MiniMessageDiagnostic.MissingPlaceholder(it.name) }

    val extra =
        tagsInInput
            .filter { it !in specNames }
            .map { MiniMessageDiagnostic.ExtraPlaceholder(it) }

    return missing + extra
}

/** Resolves placeholder names as self-closing tags for the strict malformed-tag pass. */
private fun buildPlaceholderNameResolver(placeholders: List<MiniMessagePlaceholder<*>>): TagResolver {
    if (placeholders.isEmpty()) return TagResolver.empty()
    val selfClosingTag = Tag.selfClosingInserting(emptyComponent())
    val resolvers = placeholders.map { TagResolver.resolver(it.name, selfClosingTag) }
    return TagResolver.resolver(*resolvers.toTypedArray())
}
