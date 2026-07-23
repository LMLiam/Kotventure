package io.github.lmliam.kotventure.core.virtual

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.VirtualComponent

internal class VirtualRenderState(
    vararg contexts: Any,
) {
    private val contexts = contexts.asList()
    private val activeComponents = ArrayDeque<VirtualComponent>()

    fun firstMatching(contextType: Class<*>): Any? = contexts.firstOrNull(contextType::isInstance)

    fun renderOnce(
        component: VirtualComponent,
        render: () -> Component,
    ): Component {
        if (activeComponents.any { it === component }) return component
        activeComponents.addLast(component)

        return try {
            render()
        } finally {
            // Rendering nests strictly: a virtual component's render block always completes
            // before an ancestor's does, so the popped entry must be this call's own component.
            check(activeComponents.removeLast() === component) {
                "Virtual component rendering completed out of order."
            }
        }
    }
}
