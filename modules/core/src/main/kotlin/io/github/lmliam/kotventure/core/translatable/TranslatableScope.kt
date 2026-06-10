package io.github.lmliam.kotventure.core.translatable

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Scope for configuring a translatable component with fallback text, arguments, style, and children.
 */
@KotventureDslMarker
public interface TranslatableScope : ComponentScope {
    /**
     * Applies fallback text for clients that cannot resolve the translation key.
     */
    public fun fallback(fallback: String)

    /**
     * Appends a component-like translation argument.
     */
    public fun arg(value: ComponentLike)

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
