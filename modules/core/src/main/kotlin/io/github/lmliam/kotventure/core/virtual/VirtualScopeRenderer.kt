package io.github.lmliam.kotventure.core.virtual

import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.VirtualComponentRenderer

/**
 * Adapts a [VirtualRenderScope] block to Adventure's [VirtualComponentRenderer].
 *
 * This type is a data class because Adventure includes the renderer in virtual-component equality. Two instances are
 * equal when they hold the same render block and fallback text.
 *
 * @param C the render context type.
 * @property renderBlock builds the rendered component from a context.
 * @property fallbackText the text used when the component is serialised before rendering.
 */
internal data class VirtualScopeRenderer<C : Any>(
    val renderBlock: VirtualRenderBlock<C>,
    val fallbackText: String,
) : VirtualComponentRenderer<C> {
    override fun apply(context: C): ComponentLike = VirtualRenderBuilder(context).apply(renderBlock).build()

    override fun fallbackString(): String = fallbackText
}
