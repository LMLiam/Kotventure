package io.github.lmliam.kotventure.minimessage

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Receiver for the [MiniTemplate.invoke] lambda that binds values to declared placeholders.
 *
 * Each call to [bind] records the typed value for one placeholder; the value type is enforced at
 * compile time by the generic constraint on [MiniMessagePlaceholder]. After the lambda completes,
 * [MiniTemplate.invoke] validates that every required placeholder was bound before rendering.
 */
@KotventureDslMarker
public interface MiniTemplateBindingScope {
    /**
     * Binds [value] to [placeholder].
     *
     * The value type is compile-checked against the placeholder's declared type parameter, inheriting
     * the typing guarantee from [MiniMessageResolverScope.resolve].
     *
     * @param placeholder a descriptor declared on the owning template.
     * @param value the value to substitute for this placeholder's tag in the markup.
     */
    public fun <T : Any> bind(
        placeholder: MiniMessagePlaceholder<T>,
        value: T,
    )
}
