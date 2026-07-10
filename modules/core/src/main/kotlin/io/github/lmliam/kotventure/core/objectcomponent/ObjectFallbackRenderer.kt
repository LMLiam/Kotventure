package io.github.lmliam.kotventure.core.objectcomponent

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ObjectComponent
import net.kyori.adventure.text.renderer.TranslatableComponentRenderer

internal object ObjectFallbackRenderer : TranslatableComponentRenderer<Unit>() {
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
