package io.github.lmliam.kotventure.minimessage.validation

import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

private val lenientMiniMessage =
    MiniMessage.miniMessage()

private val standardTagResolver =
    TagResolver.standard()

/**
 * Detects declared placeholders missing from [input] and undeclared custom tags present in [input].
 */
internal fun validatePlaceholderUsage(
    input: String,
    placeholders: List<MiniMessagePlaceholder<*>>,
): List<MiniMessageDiagnostic> {
    val declaredNames =
        placeholders.mapTo(linkedSetOf()) {
            it.name
        }

    val recorder =
        PlaceholderRecordingResolver(
            declaredNames = declaredNames,
            standardTags = standardTagResolver,
        )

    /*
     * Adventure gives the last resolver priority. The recorder must therefore come last so it can observe every tag.
     * It returns null, allowing the standard resolver to handle recognised standard tags afterward.
     */
    val resolver =
        TagResolver.resolver(
            standardTagResolver,
            recorder,
        )

    return try {
        lenientMiniMessage.deserialize(input, resolver)

        buildPlaceholderMismatchDiagnostics(
            placeholders = placeholders,
            declaredNames = declaredNames,
            encounteredNames = recorder.encounteredNames,
        )
    } catch (exception: RuntimeException) {
        listOf(
            exception.toValidationEngineFailure(
                fallbackMessage = "MiniMessage placeholder validation failed.",
            ),
        )
    }
}

private fun buildPlaceholderMismatchDiagnostics(
    placeholders: List<MiniMessagePlaceholder<*>>,
    declaredNames: Set<String>,
    encounteredNames: Set<String>,
): List<MiniMessageDiagnostic> =
    buildList {
        placeholders.forEach { placeholder ->
            if (placeholder.name !in encounteredNames) {
                add(
                    MiniMessageDiagnostic.MissingPlaceholder(
                        name = placeholder.name,
                    ),
                )
            }
        }

        encounteredNames.forEach { name ->
            if (name !in declaredNames) {
                add(
                    MiniMessageDiagnostic.ExtraPlaceholder(
                        name = name,
                    ),
                )
            }
        }
    }
