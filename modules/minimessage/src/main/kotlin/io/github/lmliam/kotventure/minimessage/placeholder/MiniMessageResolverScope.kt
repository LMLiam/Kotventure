package io.github.lmliam.kotventure.minimessage.placeholder

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Declares placeholder resolvers for one MiniMessage deserialisation.
 *
 * Names must match `[!?#]?[a-z0-9_-]+`. Each name can occur one time in the scope. Resolvers are local to the current
 * deserialisation and do not change the shared Adventure parser.
 */
@KotventureDslMarker
public interface MiniMessageResolverScope {
    /**
     * Resolves [name] to [value], which Adventure parses as MiniMessage markup.
     *
     * Use [unparsed] when the value is untrusted or must remain literal text.
     *
     * @throws IllegalArgumentException when [name] is invalid or already declared in this scope.
     */
    public fun parsed(
        name: String,
        value: String,
    )

    /**
     * Resolves [name] to literal [value].
     *
     * MiniMessage does not interpret tags or escapes in the value.
     *
     * @throws IllegalArgumentException when [name] is invalid or already declared in this scope.
     */
    public fun unparsed(
        name: String,
        value: String,
    )

    /**
     * Resolves [name] to the component from [value].
     *
     * @throws IllegalArgumentException when [name] is invalid or already declared in this scope.
     */
    public fun component(
        name: String,
        value: ComponentLike,
    )

    /**
     * Builds a component with [init] and resolves [name] to that component.
     *
     * @throws IllegalArgumentException when [name] is invalid or already declared in this scope.
     */
    public fun component(
        name: String,
        init: ComponentScope.() -> Unit,
    )

    /**
     * Resolves [placeholder] to [value] with the descriptor's typed strategy.
     *
     * [String], [Number], and [Boolean] values become literal text. [ComponentLike] values retain their component
     * structure.
     *
     * @throws IllegalArgumentException when the placeholder name is already declared in this scope.
     */
    public fun <T : Any> resolve(
        placeholder: MiniMessagePlaceholder<T>,
        value: T,
    )
}
