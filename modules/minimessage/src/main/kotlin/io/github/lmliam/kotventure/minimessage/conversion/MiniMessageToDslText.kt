package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.TextComponent

internal fun KotlinSourceBuilder.appendText(component: TextComponent) {
    val text = component.content()
    val hasBlockBody = hasDslOutput(component.style()) || component.children().isNotEmpty()

    if (!hasBlockBody) {
        line("text(\"${escapeKotlinString(text)}\")")
        return
    }

    val header = if (text.isEmpty()) "text" else "text(\"${escapeKotlinString(text)}\")"
    block(header) {
        appendStyle(component.style())
        component.children().forEach { appendComponent(it) }
    }
}
