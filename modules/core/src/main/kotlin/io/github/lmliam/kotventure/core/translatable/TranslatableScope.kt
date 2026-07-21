package io.github.lmliam.kotventure.core.translatable

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.ComponentLike

/**
 * Configures the fallback, arguments, style, and children of a translatable component.
 *
 * Argument functions append values in call order. The client applies those arguments when it resolves the
 * translation key.
 */
@KotventureDslMarker
public interface TranslatableScope : ComponentScope {
    /**
     * Sets the text that a client can use when it cannot resolve the translation key.
     *
     * @throws IllegalStateException when the fallback is already set in this block.
     */
    public fun fallback(fallback: String)

    /**
     * Appends a component-like translation argument.
     */
    public fun arg(value: ComponentLike)

    /**
     * Builds an inline component translation argument from [init] and appends it.
     */
    public fun arg(init: TextScope.() -> Unit)

    /**
     * Appends a boolean translation argument.
     */
    public fun arg(value: Boolean)

    /**
     * Appends a numeric translation argument.
     */
    public fun arg(value: Number)

    /**
     * Appends multiple component-like translation arguments in declaration order.
     */
    public fun args(vararg values: ComponentLike)

    /**
     * Appends multiple boolean translation arguments in declaration order.
     */
    public fun args(vararg values: Boolean)

    /**
     * Appends multiple numeric translation arguments in declaration order.
     */
    public fun args(vararg values: Number)
}
