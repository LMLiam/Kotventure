package io.github.lmliam.kotventure.minimessage.template

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder

/**
 * Binds values for one template render.
 *
 * The placeholder type constrains the value type at compile time. A binding fails immediately if its placeholder
 * belongs to another template or already has a value. The render fails after the block if any required placeholder has
 * no value.
 */
@KotventureDslMarker
public interface MiniTemplateBindingScope {
    /**
     * Records [value] for [placeholder] in this render.
     *
     * @throws IllegalArgumentException when [placeholder] belongs to another template or already has a value.
     */
    public fun <T : Any> bind(
        placeholder: MiniMessagePlaceholder<T>,
        value: T,
    )
}

/**
 * Binds [value] to this placeholder for the current render.
 *
 * @throws IllegalArgumentException when this descriptor is not the instance that the current template declared, or
 * when it already has a value in this render.
 */
context(scope: MiniTemplateBindingScope)
public infix fun <T : Any> MiniMessagePlaceholder<T>.bind(value: T): Unit = scope.bind(this, value)
