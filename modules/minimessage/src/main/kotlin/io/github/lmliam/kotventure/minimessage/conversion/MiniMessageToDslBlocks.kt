package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.Component

/**
 * Emits one structured component call, with a block only when the component has body content.
 */
internal fun KotlinSourceBuilder.appendStructured(
    header: String,
    component: Component,
    hasExtraBody: Boolean = false,
    body: KotlinSourceBuilder.() -> Unit = {},
) {
    val needsBlock = hasExtraBody || hasDslOutput(component.style()) || component.children().isNotEmpty()
    if (!needsBlock) {
        line(header)
        return
    }

    block(header) {
        emitComponentBody(component, body)
    }
}

/**
 * Emits a structured component call with multiline [arguments].
 *
 * The call has a block when [body], style, or children produce output.
 */
internal fun KotlinSourceBuilder.appendStructuredArguments(
    opener: String,
    arguments: List<KotlinSourceBuilder.() -> Unit>,
    component: Component,
    hasExtraBody: Boolean = false,
    body: KotlinSourceBuilder.() -> Unit = {},
) {
    openArguments(opener, arguments.map { { it() } })
    val needsBlock = hasExtraBody || hasDslOutput(component.style()) || component.children().isNotEmpty()
    if (!needsBlock) {
        line(")")
        return
    }

    block(")") {
        emitComponentBody(component, body)
    }
}

/**
 * Emits a labelled component argument block.
 */
internal fun KotlinSourceBuilder.appendComponentArgument(
    label: String,
    component: Component,
) = block(label) { appendRoot(component) }

/**
 * Emits extra [body] content, style, and child components in that order.
 */
private fun KotlinSourceBuilder.emitComponentBody(
    component: Component,
    body: KotlinSourceBuilder.() -> Unit,
) {
    body()
    appendStyle(component.style())
    component.children().forEach { appendComponent(it) }
}
