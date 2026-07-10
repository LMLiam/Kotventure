package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TranslationArgument

internal fun KotlinSourceBuilder.appendArgument(argument: TranslationArgument) {
    when (val value = argument.value()) {
        is Component -> appendComponentArgument("arg", value)
        is Boolean -> line("arg($value)")
        is Number -> line("arg(${kotlinNumberLiteral(value)})")
        else ->
            conversionError(
                "miniToDsl cannot represent ${value::class.qualifiedName} translatable argument.",
            )
    }
}
