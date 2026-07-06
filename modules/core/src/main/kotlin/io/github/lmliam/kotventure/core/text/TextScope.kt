package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.color.ColorGradient
import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.format.TextColor

/**
 * Scope for configuring text-specific content on a text component.
 */
@KotventureDslMarker
public interface TextScope : ComponentScope {
    /**
     * Sets the text content of the component being configured.
     *
     * @throws IllegalStateException when the content is already set in this block.
     */
    public fun content(value: String)

    /**
     * Applies [gradient] across this text component's content, one code point at a time.
     *
     * @throws IllegalStateException when a gradient is already set in this block, or — at build time — when the
     *         component's content is empty and there is no text to color.
     */
    public fun gradient(gradient: ColorGradient)

    /**
     * Applies a gradient built from [stops] across this text component's content, one code point at a time.
     *
     * @throws IllegalStateException when a gradient is already set in this block, or — at build time — when the
     *         component's content is empty and there is no text to color.
     */
    public fun gradient(vararg stops: TextColor)
}
