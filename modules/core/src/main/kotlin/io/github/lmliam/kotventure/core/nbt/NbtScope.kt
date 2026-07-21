package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.ComponentLike

/**
 * Configures the value handling, separator, style, and children of an NBT component.
 */
@KotventureDslMarker
public interface NbtScope : ComponentScope {
    /**
     * Sets whether the client parses each selected NBT value as component JSON.
     *
     * The default value is `false`.
     *
     * @throws IllegalStateException when interpretation is already set in this block.
     */
    public fun interpret(interpret: Boolean)

    /**
     * Sets the component that separates multiple selected NBT values.
     *
     * @throws IllegalStateException when the separator is already set in this block.
     */
    public fun separator(separator: ComponentLike)

    /**
     * Builds an inline text component and sets it as the separator between selected NBT values.
     *
     * @throws IllegalStateException when the separator is already set in this block.
     */
    public fun separator(init: TextScope.() -> Unit)
}
