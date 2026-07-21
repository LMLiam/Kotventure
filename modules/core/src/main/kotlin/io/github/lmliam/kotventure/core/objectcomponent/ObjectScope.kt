package io.github.lmliam.kotventure.core.objectcomponent

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures the fallback, style, and children of an object component.
 */
@KotventureDslMarker
public interface ObjectScope : ComponentScope {
    /**
     * Sets the component that a fallback renderer can use in place of the object.
     *
     * A null value clears the fallback. The object component does not select a fallback automatically from client
     * capabilities.
     *
     * @throws IllegalStateException when the fallback is already set in this block.
     */
    public fun fallback(fallback: ComponentLike?)

    /**
     * Builds an inline component and sets it as the object fallback.
     *
     * @throws IllegalStateException when the fallback is already set in this block.
     */
    public fun fallback(init: ComponentScope.() -> Unit)
}
