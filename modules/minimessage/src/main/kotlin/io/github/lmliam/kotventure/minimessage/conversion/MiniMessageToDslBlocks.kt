package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.Component

/**
 * Emits one structured component call, with a block only when the component has body content.
 */
internal fun KotlinSourceBuilder.appendStructured(
    header: String,
    component: Component,
    hasAdditionalBody: Boolean = false,
    body: KotlinSourceBuilder.() -> Unit = {},
) {
    if (!component.requiresBlock(hasAdditionalBody)) {
        line(header)
        return
    }

    block(header) {
        appendComponentBody(component, body)
    }
}

/**
 * Emits a structured component call with multiline [arguments].
 *
 * The call has a trailing lambda when [body], style, or children produce output.
 */
internal fun KotlinSourceBuilder.appendStructuredCall(
    function: String,
    arguments: List<KotlinSourceBuilder.() -> Unit>,
    component: Component,
    hasAdditionalBody: Boolean = false,
    body: KotlinSourceBuilder.() -> Unit = {},
) {
    if (component.requiresBlock(hasAdditionalBody)) {
        call(function, arguments) {
            appendComponentBody(component, body)
        }
    } else {
        call(function, arguments)
    }
}

/** Emits a labelled component argument block. */
internal fun KotlinSourceBuilder.appendComponentArgument(
    label: String,
    component: Component,
) = block(label) { appendRoot(component) }

/** Emits extra [body] content, style, and child components in that order. */
private fun KotlinSourceBuilder.appendComponentBody(
    component: Component,
    body: KotlinSourceBuilder.() -> Unit,
) {
    body()
    appendStyle(component.style())
    component.children().forEach { appendComponent(it) }
}

private fun Component.requiresBlock(hasAdditionalBody: Boolean): Boolean =
    hasAdditionalBody || hasDslOutput(style()) || children().isNotEmpty()
