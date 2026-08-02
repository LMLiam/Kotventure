package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.VirtualComponent

/**
 * Configures the fallback and rendered appearances of a [VirtualComponent].
 *
 * The [render] block builds dynamic content from a render context. The [fallback] is the static representation used
 * before a renderer resolves the component, such as during serialisation or console output. Static style and children
 * belong in the fallback block; this scope itself exposes only the two appearance slots.
 *
 * @sample io.github.lmliam.kotventure.core.virtual.virtualSample
 *
 * @param C the render context type, for example the viewing player.
 */
@KotventureDslMarker
public interface VirtualScope<C : Any> {
    /**
     * Sets the plain-text fallback used before the component is rendered.
     *
     * This form and the block form of [fallback] share one write-once slot.
     *
     * @param text the fallback text.
     * @throws IllegalStateException when this block already set a fallback.
     */
    public fun fallback(text: String)

    /**
     * Builds the styled fallback used before the component is rendered.
     *
     * The block configures the fallback's static style and children. This form and the text form of [fallback] share
     * one write-once slot.
     *
     * @sample io.github.lmliam.kotventure.core.virtual.virtualStyledFallbackSample
     *
     * @param init styles the fallback and appends its children.
     * @throws IllegalStateException when this block already set a fallback.
     */
    public fun fallback(init: ComponentScope.() -> Unit)

    /**
     * Sets the block that builds content from the current render context.
     *
     * Adventure or [Component.render] invokes [init] when it resolves the component. The block can run more
     * than once and receives a new [VirtualRenderScope] for each invocation.
     *
     * @param init builds content from [VirtualRenderScope.context].
     * @throws IllegalStateException when this block already set the render block.
     */
    public fun render(init: VirtualRenderScope<C>.() -> Unit)
}
