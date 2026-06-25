package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

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
    public fun <T : ComponentLike> separator(component: T)

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
    public fun <T : ComponentLike> lastSeparator(component: T)

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
    public fun <T : ComponentLike> prefix(component: T)

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
    public fun <T : ComponentLike> suffix(component: T)
}
