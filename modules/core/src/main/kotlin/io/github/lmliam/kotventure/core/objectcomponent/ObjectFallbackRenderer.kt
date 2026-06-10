package io.github.lmliam.kotventure.core.objectcomponent

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer

/**
 * Renders object components to their fallback components where a fallback is configured.
 */
public fun Component.renderObjectFallbacks(): Component = ObjectFallbackRenderer.render(this, Unit)

private object ObjectFallbackRenderer : TranslatableComponentRenderer<Unit>() {
    override fun renderObject(
        component: ObjectComponent,
        context: Unit,
    ): Component {
        val renderedObject = super.renderObject(component, context)
        val fallback = component.fallback() ?: return renderedObject
        val renderedFallback = render(fallback, context)
        val renderedChildren = renderedObject.children()

        return renderedChildren.fold(renderedFallback) { parent, child -> parent.append(child) }
    }
}
