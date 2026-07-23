package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.VirtualComponent
import net.kyori.adventure.text.format.Style

/**
 * Builds a [VirtualComponent] from the slots in [VirtualScope].
 *
 * A render block is required.
 * The fallback has no content, style, or children when it is not set.
 *
 * @param C the render context type.
 * @property contextType the render context class that the component reports.
 */
internal class VirtualBuilder<C : Any>(
    private val contextType: Class<C>,
) : VirtualScope<C> {
    private var fallback: Fallback? by once()
    private var render: (VirtualRenderScope<C>.() -> Unit)? by once()

    override fun fallback(text: String) {
        fallback = Fallback(text)
    }

    override fun fallback(build: ComponentScope.() -> Unit) {
        val component = component(build)
        fallback = Fallback(style = component.style(), children = component.children())
    }

    override fun render(build: VirtualRenderScope<C>.() -> Unit) {
        render = build
    }

    fun build(): VirtualComponent {
        val render = checkNotNull(render) { "'render' is required" }
        val resolvedFallback = fallback ?: Fallback()
        val renderer = VirtualScopeRenderer(render, resolvedFallback.content)

        return Component
            .virtual(contextType, renderer, resolvedFallback.style)
            .children(resolvedFallback.children) as VirtualComponent
    }

    private data class Fallback(
        val content: String = "",
        val style: Style = Style.empty(),
        val children: List<Component> = emptyList(),
    )
}
