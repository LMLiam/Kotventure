package io.github.lmliam.kotventure.minimessage.conversion

import io.github.lmliam.kotventure.core.selector.parseSelector
import net.kyori.adventure.text.KeybindComponent
import net.kyori.adventure.text.ScoreComponent
import net.kyori.adventure.text.SelectorComponent
import net.kyori.adventure.text.TranslatableComponent

internal fun KotlinSourceBuilder.appendTranslatable(component: TranslatableComponent) {
    val fallback = component.fallback()
    val arguments = component.arguments()

    appendStructured(
        header = "translatable(${quoted(component.key())})",
        component = component,
        hasExtraBody = fallback != null || arguments.isNotEmpty(),
    ) {
        fallback?.let { line("fallback(${quoted(it)})") }
        arguments.forEach { appendArgument(it) }
    }
}

internal fun KotlinSourceBuilder.appendKeybind(component: KeybindComponent) =
    appendStructured(
        header = "keybind(${quoted(component.keybind())})",
        component = component,
    ) {}

internal fun KotlinSourceBuilder.appendScore(component: ScoreComponent) {
    appendStructured(
        header = "score(${quoted(component.name())}, ${quoted(component.objective())})",
        component = component,
    ) {}
}

internal fun KotlinSourceBuilder.appendSelector(component: SelectorComponent) {
    val selector = parseSelector(component.pattern())
    val separator = component.separator()

    appendStructuredArguments(
        opener = "selector(",
        arguments = listOf({ appendEntitySelector(selector) }),
        component = component,
        hasExtraBody = separator != null,
    ) {
        separator?.let { appendComponentArgument("separator", it) }
    }
}
