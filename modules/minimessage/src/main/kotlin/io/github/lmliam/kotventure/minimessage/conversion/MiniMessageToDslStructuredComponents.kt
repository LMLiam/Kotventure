package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.text.ScoreComponent
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.TranslatableComponent

internal fun KotlinSourceBuilder.appendTranslatable(component: TranslatableComponent) {
    val fallback = component.fallback()
    val arguments = component.arguments()
    appendStructured(
        header = "translatable(\"${escapeKotlinString(component.key())}\")",
        component = component,
        hasExtraBody = fallback != null || arguments.isNotEmpty(),
    ) {
        fallback?.let { line("fallback(\"${escapeKotlinString(it)}\")") }
        arguments.forEach { appendArgument(it) }
    }
}

internal fun KotlinSourceBuilder.appendKeybind(component: KeybindComponent) {
    appendStructured("keybind(\"${escapeKotlinString(component.keybind())}\")", component) {}
}

internal fun KotlinSourceBuilder.appendScore(component: ScoreComponent) {
    val name = escapeKotlinString(component.name())
    val objective = escapeKotlinString(component.objective())
    appendStructured("score(\"$name\", \"$objective\")", component) {}
}

internal fun KotlinSourceBuilder.appendSelector(component: SelectorComponent) {
    val separator = component.separator()
    appendStructured(
        header = "selector(${entitySelectorLiteral(component.pattern())})",
        component = component,
        hasExtraBody = separator != null,
    ) {
        separator?.let { appendComponentArgument("separator", it) }
    }
}
