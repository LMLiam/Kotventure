package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.Component

/**
 * Builds virtual-component content from a render [context] of type [C].
 *
 * Adventure or [Component.render] supplies [context] when it resolves the virtual component. A render block can run
 * more than once and receives a new scope for each invocation. The scope exposes the same style and child-component
 * operations as a `component { }` block.
 *
 * @param C the render context type, for example the viewing player.
 * @sample io.github.lmliam.kotventure.core.virtual.virtualSample
 */
@KotventureDslMarker
public interface VirtualRenderScope<C : Any> : ComponentScope {
    /**
     * The context supplied for the current render.
     */
    public val context: C
}

internal typealias VirtualRenderBlock<C> = VirtualRenderScope<C>.() -> Unit
