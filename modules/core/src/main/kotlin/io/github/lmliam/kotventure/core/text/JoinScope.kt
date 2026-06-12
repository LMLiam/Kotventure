package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.Component

/**
 * Scope for configuring how a sequence of components is joined.
 */
@KotventureDslMarker
public interface JoinScope {
    /**
     * Sets the separator between adjacent joined components to a text component with [value], configured by [init].
     */
    public fun separator(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Sets the separator between adjacent joined components.
     */
    public fun separator(component: Component)

    /**
     * Sets the component inserted before the final joined component instead of the separator, as a text
     * component with [value] configured by [init].
     */
    public fun lastSeparator(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Sets the component inserted before the final joined component instead of the separator.
     */
    public fun lastSeparator(component: Component)

    /**
     * Sets the component prepended to the joined result to a text component with [value], configured by [init].
     */
    public fun prefix(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Sets the component prepended to the joined result.
     */
    public fun prefix(component: Component)

    /**
     * Sets the component appended to the joined result to a text component with [value], configured by [init].
     */
    public fun suffix(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Sets the component appended to the joined result.
     */
    public fun suffix(component: Component)
}
