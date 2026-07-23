package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentBuilder
import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.once
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.VirtualComponent
import net.kyori.adventure.text.format.Style

/**
 * Collects the [VirtualScope] slots and builds the Adventure [VirtualComponent].
 *
 * The builder holds a write-once fallback and a write-once render block. The [build] step requires the render block,
 * so a block without a render call fails fast.
 *
 * @param C the render context type.
 * @property contextType the reified render context class that the component reports.
 */
internal class VirtualBuilder<C : Any>(
    private val contextType: Class<C>,
) : VirtualScope<C> {
    private var fallback: ResolvedFallback? by once()
    private var render: (VirtualRenderScope<C>.() -> Unit)? by once()

    override fun fallback(text: String) {
        fallback = ResolvedFallback(text, Style.empty(), emptyList())
    }

    override fun fallback(build: ComponentScope.() -> Unit) {
        val stand = ComponentBuilder(Component.text()).apply(build).build()
        fallback = ResolvedFallback("", stand.style(), stand.children())
    }

    override fun render(build: VirtualRenderScope<C>.() -> Unit) {
        render = build
    }

    fun build(): VirtualComponent {
        val body = checkNotNull(render) { "a render block is required" }
        val stand = fallback ?: ResolvedFallback("", Style.empty(), emptyList())
        val renderer = VirtualScopeRenderer(body, stand.string)
        return Component.virtual(contextType, renderer, stand.style).children(stand.children) as VirtualComponent
    }

    /**
     * The resolved fallback: the plain-text stand-in and the style and children of the virtual node.
     */
    private data class ResolvedFallback(
        val string: String,
        val style: Style,
        val children: List<Component>,
    )
}
