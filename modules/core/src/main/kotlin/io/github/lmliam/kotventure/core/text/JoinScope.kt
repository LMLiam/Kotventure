package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures separators and wrappers for a component join operation.
 *
 * The normal separator applies between adjacent elements. [lastSeparator] replaces it before the final element.
 * [prefix] and [suffix] also apply to empty and one-element inputs. Each setting is optional and write-once.
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
     * Sets the text component used before the final element instead of the normal separator.
     *
     * @throws IllegalStateException when the last separator is already set in this block.
     */
    public fun lastSeparator(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Sets [component] before the final element instead of the normal separator.
     *
     * @throws IllegalStateException when the last separator is already set in this block.
     */
    public fun <T : ComponentLike> lastSeparator(component: T)

    /**
     * Sets a text component at the start of the result, including an empty result.
     *
     * @throws IllegalStateException when the prefix is already set in this block.
     */
    public fun prefix(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Sets [component] at the start of the result, including an empty result.
     *
     * @throws IllegalStateException when the prefix is already set in this block.
     */
    public fun <T : ComponentLike> prefix(component: T)

    /**
     * Sets a text component at the end of the result, including an empty result.
     *
     * @throws IllegalStateException when the suffix is already set in this block.
     */
    public fun suffix(
        value: String,
        init: TextScope.() -> Unit = {},
    )

    /**
     * Sets [component] at the end of the result, including an empty result.
     *
     * @throws IllegalStateException when the suffix is already set in this block.
     */
    public fun <T : ComponentLike> suffix(component: T)
}
