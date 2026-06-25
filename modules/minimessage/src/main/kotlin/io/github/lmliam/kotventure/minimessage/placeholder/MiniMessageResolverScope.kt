package io.github.lmliam.kotventure.minimessage.placeholder

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures placeholder resolvers applied while parsing MiniMessage markup.
 */
@KotventureDslMarker
public interface MiniMessageResolverScope {
    /**
     * Inserts [value] as parsed MiniMessage markup for the placeholder named [name].
     */
    public fun parsed(
        name: String,
        value: String,
    )

    /**
     * Inserts [value] as literal text for the placeholder named [name].
     */
    public fun unparsed(
        name: String,
        value: String,
    )

    /**
     * Inserts [value] as a self-closing component placeholder for the placeholder named [name].
     */
    public fun component(
        name: String,
        value: ComponentLike,
    )

    /**
     * Builds a component placeholder named [name] from a Kotventure component DSL block.
     */
    public fun component(
        name: String,
        init: ComponentScope.() -> Unit,
    )

    /**
     * Resolves [placeholder] to [value] using the placeholder's typed binding strategy.
     *
     * [String], [Number], and [Boolean] values are inserted as literal text. [ComponentLike] values are inserted as
     * component placeholders.
     */
    public fun <T : Any> resolve(
        placeholder: MiniMessagePlaceholder<T>,
        value: T,
    )
}
