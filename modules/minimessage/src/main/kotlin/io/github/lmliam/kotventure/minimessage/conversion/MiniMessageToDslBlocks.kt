package io.github.lmliam.kotventure.minimessage.conversion

import net.kyori.adventure.text.Component

/**
 * Helpers for emitting structured DSL blocks for components.
 *
 * These functions are small, composable blocks. Small helpers emit the body without duplicate logic. A vararg overload
 * supplies arguments to callers.
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
 * Emits a structured-component call with multi-line arguments.
 *
 * The `opener` is on its own line. Each argument has an indent and a separating comma. The output then has a closing
 * `)` or a `) { ... }` body. The body contains [body], the component's style, and its children.
 *
 * This overload accepts a List of argument-emitting lambdas to remain compatible with existing callers.
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
 * Emit a labelled component argument block, e.g. `label { ... }`.
 */
internal fun KotlinSourceBuilder.appendComponentArgument(
    label: String,
    component: Component,
) = block(label) { appendRoot(component) }

/**
 * Shared small helper that emits the common trailing content for structured blocks:
 * - optional extra body content (via [body])
 * - style emission
 * - child components
 */
private fun KotlinSourceBuilder.emitComponentBody(
    component: Component,
    body: KotlinSourceBuilder.() -> Unit,
) {
    body()
    appendStyle(component.style())
    component.children().forEach { appendComponent(it) }
}
