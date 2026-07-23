package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.VirtualComponent

/**
 * Configures the two appearances of a [VirtualComponent].
 *
 * A virtual component has two forms. The [render] block builds the content that a platform shows at display time. The
 * [fallback] is the stand-in that a serialiser or a console shows when no platform renders the component. Set a style
 * or add children on the [fallback] block, not on this scope. This scope only holds the [fallback] and [render] slots.
 *
 * @sample io.github.lmliam.kotventure.core.virtual.virtualSample
 *
 * @param C the render context type, for example the viewing player.
 */
@KotventureDslMarker
public interface VirtualScope<C : Any> {
    /**
     * Sets the plain-text stand-in shown when no platform renders the component.
     *
     * This form and the [fallback] block form share one write-once slot.
     *
     * @param text the stand-in text.
     * @throws IllegalStateException when a fallback is already set in this block.
     */
    public fun fallback(text: String)

    /**
     * Builds a styled stand-in shown when no platform renders the component.
     *
     * The [build] block sets the style and children of the stand-in. This form and the [fallback] text form share one
     * write-once slot.
     *
     * @sample io.github.lmliam.kotventure.core.virtual.virtualStyledFallbackSample
     *
     * @param build styles the stand-in and appends any children.
     * @throws IllegalStateException when a fallback is already set in this block.
     */
    public fun fallback(build: ComponentScope.() -> Unit)

    /**
     * Sets the block that builds the rendered content from the render context.
     *
     * The platform calls [build] at display time with the current context, for example the viewing player. The block
     * can run more than once. The `core` module never calls it.
     *
     * @param build builds the rendered content from the [VirtualRenderScope.context].
     * @throws IllegalStateException when a render block is already set in this block.
     */
    public fun render(build: VirtualRenderScope<C>.() -> Unit)
}
