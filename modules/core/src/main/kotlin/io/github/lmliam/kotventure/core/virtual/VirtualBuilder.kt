package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.VirtualComponent
import net.kyori.adventure.text.format.Style

/**
 * Collects [VirtualScope] state and builds one [VirtualComponent].
 *
 * The render block is required.
 * An omitted fallback produces an empty fallback string with no style or children.
 *
 * @param C the render context type.
 * @property contextType the context class exposed by the resulting virtual component
 */
internal class VirtualBuilder<C : Any>(
    private val contextType: Class<C>,
) : VirtualScope<C> {
    private var fallback: VirtualFallback? by once()
    private var renderBlock: VirtualRenderBlock<C>? by once { "'render' is already set." }

    override fun fallback(text: String) {
        fallback = VirtualFallback(content = text)
    }

    override fun fallback(init: ComponentScope.() -> Unit) {
        fallback = buildVirtualFallback(init)
    }

    override fun render(init: VirtualRenderScope<C>.() -> Unit) {
        renderBlock = init
    }

    internal fun build(): VirtualComponent {
        val renderBlock = checkNotNull(renderBlock) { "'render' is not set." }
        val fallback = fallback ?: VirtualFallback()
        val renderer =
            VirtualScopeRenderer(
                renderBlock = renderBlock,
                fallbackText = fallback.content,
            )

        return Component
            .virtual(contextType, renderer, fallback.style)
            .withChildren(fallback.children)
    }
}

// Adventure preserves the virtual subtype, but the Java API exposes Component as the return type
private fun VirtualComponent.withChildren(children: List<Component>): VirtualComponent =
    if (children.isEmpty()) this else children(children) as VirtualComponent
