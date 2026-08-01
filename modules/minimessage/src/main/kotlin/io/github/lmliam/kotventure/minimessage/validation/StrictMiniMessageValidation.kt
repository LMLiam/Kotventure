package io.github.lmliam.kotventure.minimessage.validation

import io.github.lmliam.kotventure.core.component.emptyComponent
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.ParsingException
import net.kyori.adventure.text.minimessage.tag.Tag
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

private val strictMiniMessage =
    MiniMessage
        .builder()
        .strict(true)
        .build()

private val standardTagResolver =
    TagResolver.standard()

private val placeholderValidationTag =
    Tag.selfClosingInserting(emptyComponent())

/**
 * Returns the first strict-parser diagnostic for [input], or `null` when strict parsing succeeds.
 */
internal fun validateStrictMarkup(
    input: String,
    placeholders: List<MiniMessagePlaceholder<*>>,
): MiniMessageDiagnostic? {
    val resolver =
        TagResolver.resolver(
            standardTagResolver,
            placeholders.toPlaceholderNameResolver(),
        )

    return try {
        strictMiniMessage.deserialize(input, resolver)
        null
    } catch (exception: ParsingException) {
        exception.toMalformedTagDiagnostic()
    } catch (exception: RuntimeException) {
        exception.toValidationEngineFailure(
            fallbackMessage = "Strict MiniMessage validation failed.",
        )
    }
}

/**
 * Resolves declared placeholder names as empty self-closing tags during strict validation.
 */
private fun List<MiniMessagePlaceholder<*>>.toPlaceholderNameResolver(): TagResolver =
    TagResolver.resolver(
        map { placeholder ->
            TagResolver.resolver(
                placeholder.name,
                placeholderValidationTag,
            )
        },
    )
