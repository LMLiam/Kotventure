package io.github.lmliam.kotventure.core.virtual

import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.VirtualComponentRenderer

/**
 * Adapts a [VirtualRenderScope] block to Adventure's [VirtualComponentRenderer].
 *
 * The type is a `data class` on purpose. Two renderers are equal when they hold the same [render] block and the same
 * [fallback]. Adventure compares the renderer as part of virtual component equality, so this equality lets a test
 * compare a DSL component with a raw `net.kyori` component.
 *
 * @param C the render context type.
 * @property render the block that builds the component content from the context.
 * @property fallback the text that a serialiser shows when no platform renders the component.
 */
@PublishedApi
internal data class VirtualScopeRenderer<C : Any>(
    val render: VirtualRenderScope<C>.() -> Unit,
    val fallback: String,
) : VirtualComponentRenderer<C> {
    override fun apply(context: C): ComponentLike = VirtualRenderBuilder(context).apply(render).build()

    override fun fallbackString(): String = fallback
}
