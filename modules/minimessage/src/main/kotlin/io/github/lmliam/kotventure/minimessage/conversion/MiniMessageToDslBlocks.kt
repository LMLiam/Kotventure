package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.Component

internal fun KotlinSourceBuilder.appendStructured(
    header: String,
    component: Component,
    hasExtraBody: Boolean = false,
    body: KotlinSourceBuilder.() -> Unit,
) {
    val hasStyle = hasDslOutput(component.style())
    if (!hasExtraBody && !hasStyle && component.children().isEmpty()) {
        line(header)
        return
    }

    block(header) {
        body()
        appendStyle(component.style())
        component.children().forEach { appendComponent(it) }
    }
}

internal fun KotlinSourceBuilder.appendComponentArgument(
    label: String,
    component: Component,
) {
    block(label) { appendRoot(component) }
}
