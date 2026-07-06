package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Scope for configuring how a sequence of components is joined.
 *
 * Each part (separator, last separator, prefix, suffix) is a singleton slot: setting it twice within one block
 * throws [IllegalStateException] instead of silently overwriting the first value.
 */
@KotventureDslMarker
public interface JoinScope {
    /**
     * Sets the separator between adjacent joined components to a text component with [value], configured by [init].
     *
     * @throws IllegalStateException when the separator is already set in this block.
     */
    public fun separator(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Sets the separator between adjacent joined components.
     *
     * @throws IllegalStateException when the separator is already set in this block.
     */
    public fun <T : ComponentLike> separator(component: T)

    /**
     * Sets the component inserted before the final joined component instead of the separator, as a text
     * component with [value] configured by [init].
     *
     * @throws IllegalStateException when the last separator is already set in this block.
     */
    public fun lastSeparator(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Sets the component inserted before the final joined component instead of the separator.
     *
     * @throws IllegalStateException when the last separator is already set in this block.
     */
    public fun <T : ComponentLike> lastSeparator(component: T)

    /**
     * Sets the component prepended to the joined result to a text component with [value], configured by [init].
     *
     * @throws IllegalStateException when the prefix is already set in this block.
     */
    public fun prefix(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Sets the component prepended to the joined result.
     *
     * @throws IllegalStateException when the prefix is already set in this block.
     */
    public fun <T : ComponentLike> prefix(component: T)

    /**
     * Sets the component appended to the joined result to a text component with [value], configured by [init].
     *
     * @throws IllegalStateException when the suffix is already set in this block.
     */
    public fun suffix(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Sets the component appended to the joined result.
     *
     * @throws IllegalStateException when the suffix is already set in this block.
     */
    public fun <T : ComponentLike> suffix(component: T)
}
