package io.github.lmliam.kotventure.core.objectcomponent

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Scope for configuring an object component with fallback text, style, and children.
 */
@KotventureDslMarker
public interface ObjectScope : ComponentScope {
    /**
     * Sets the component displayed where object components are unsupported, or clears it with `null`.
     *
     * @throws IllegalStateException when the fallback is already set in this block.
     */
    public fun fallback(fallback: ComponentLike?)

    /**
     * Builds and applies an inline fallback component for clients that cannot display object components.
     *
     * @throws IllegalStateException when the fallback is already set in this block.
     */
    public fun fallback(init: ComponentScope.() -> Unit)
}
