package io.github.lmliam.kotventure.minimessage.template

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.minimessage.placeholder.MiniMessagePlaceholder

/**
 * Receiver for a template render block, binding a value to each declared placeholder.
 *
 * Inside the block, bind with the [bind] infix function: `player bind playerName`. The value type is
 * checked at compile time against the placeholder's type parameter. Binding the same placeholder twice,
 * binding a placeholder from another template, or leaving one unbound all fail when the block completes.
 */
@KotventureDslMarker
public interface MiniTemplateBindingScope {
    /** Records [value] as the binding for [placeholder]; prefer the [bind] infix form at call sites. */
    public fun <T : Any> bind(
        placeholder: MiniMessagePlaceholder<T>,
        value: T,
    )
}

/**
 * Binds [value] to this placeholder for the current render, e.g. `player bind playerName`.
 *
 * @throws IllegalArgumentException if this placeholder is not declared on the template being rendered,
 *   or has already been bound in this render.
 */
// ktlint 1.7.0 keeps the context list inline (its context-receiver-list-wrapping rule predates the
// `context(name: Type)` parameter syntax), so suppress its formatting to keep `context(...)` on its own line.
@Suppress("ktlint")
context(scope: MiniTemplateBindingScope)
public infix fun <T : Any> MiniMessagePlaceholder<T>.bind(value: T): Unit = scope.bind(this, value)
