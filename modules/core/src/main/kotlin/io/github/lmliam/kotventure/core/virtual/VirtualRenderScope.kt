package io.github.lmliam.kotventure.core.virtual

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Builds the content of a virtual component from a render [context] of type [C].
 *
 * The platform supplies the [context] and calls the render block at display time. The block can run more than once, and
 * it runs one time for each render. The scope inherits the component surface from [ComponentScope], so it accepts the
 * same style calls and child components as a `component { }` block.
 *
 * @param C the render context type, for example the viewing player.
 * @sample io.github.lmliam.kotventure.core.virtual.virtualSample
 */
@KotventureDslMarker
public interface VirtualRenderScope<C : Any> : ComponentScope {
    /**
     * The render context that the platform supplies at display time.
     */
    public val context: C
}
