package io.github.lmliam.kotventure.minimessage.template

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder

/**
 * Receives values for one template render.
 *
 * A placeholder's type constrains its value at compile time. Each descriptor must belong to the template being
 * rendered and may be bound exactly once. Rendering fails after the binding block when any required placeholder
 * remains unbound.
 */
@KotventureDslMarker
public interface MiniTemplateBindingScope {
    /**
     * Binds [value] to [placeholder] for the current render.
     *
     * @throws IllegalArgumentException when [placeholder] belongs to another template or is already bound.
     */
    public fun <T : Any> bind(
        placeholder: MiniMessagePlaceholder<T>,
        value: T,
    )
}

/**
 * Binds [value] to this placeholder for the current template render.
 *
 * @throws IllegalArgumentException when this descriptor was not declared by the current template or has already been
 * bound during this render.
 */
context(bindings: MiniTemplateBindingScope)
public infix fun <T : Any> MiniMessagePlaceholder<T>.bind(value: T): Unit =
    bindings.bind(
        placeholder = this,
        value = value,
    )
