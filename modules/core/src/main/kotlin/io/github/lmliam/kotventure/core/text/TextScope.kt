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
     * Replaces the text content of the component being configured.
     */
    public fun content(value: String)

    /**
     * Applies [gradient] across this text component's content, one code point at a time.
     */
    public fun gradient(gradient: ColorGradient)

    /**
     * Applies a gradient built from [stops] across this text component's content, one code point at a time.
     */
    public fun gradient(vararg stops: TextColor)
}
