package io.github.lmliam.kotventure.core.virtual

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.VirtualComponent

internal class VirtualRenderState(
    context: Any,
    additionalContexts: Array<out Any>,
) {
    private val contexts: List<Any> = listOf(context, *additionalContexts)
    private val activeComponents: MutableList<VirtualComponent> = mutableListOf()

    fun firstMatching(contextType: Class<*>): Any? = contexts.firstOrNull(contextType::isInstance)

    fun renderOnce(
        component: VirtualComponent,
        render: () -> Component,
    ): Component {
        if (activeComponents.any { it === component }) return component
        activeComponents += component

        return try {
            render()
        } finally {
            check(activeComponents.removeLast() === component) {
                "Virtual component rendering completed out of order."
            }
        }
    }
}
