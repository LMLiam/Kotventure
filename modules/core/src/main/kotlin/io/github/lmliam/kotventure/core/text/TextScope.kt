package io.github.lmliam.kotventure.core.text

import io.github.lmliam.kotventure.core.color.ColorGradient
import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.format.TextColor

/**
 * Configures the literal content and optional colour gradient of a text component.
 *
 * This scope also provides the style and child operations from [ComponentScope]. Content and gradient are write-once.
 */
@KotventureDslMarker
public interface TextScope : ComponentScope {
    /**
     * Sets [value] as the component's literal content.
     *
     * @throws IllegalStateException when the content is already set in this block.
     */
    public fun content(value: String)

    /**
     * Applies [gradient] to the literal content, one Unicode code point at a time.
     *
     * @throws IllegalStateException when a gradient is already set in this block. The build also throws this exception when the
     *         component's content is empty and there is no text to colour.
     */
    public fun gradient(gradient: ColorGradient)

    /**
     * Applies a gradient from [stops] to the literal content, one Unicode code point at a time.
     *
     * @throws IllegalArgumentException when [stops] contains fewer than two colours.
     * @throws IllegalStateException when a gradient is already set in this block. The build also throws this exception when the
     *         component's content is empty and there is no text to colour.
     */
    public fun gradient(vararg stops: TextColor)
}
