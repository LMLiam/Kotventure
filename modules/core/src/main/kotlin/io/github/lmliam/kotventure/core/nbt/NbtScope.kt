package io.github.lmliam.kotventure.core.nbt

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.text.TextScope
import net.kyori.adventure.text.ComponentLike

/**
 * Scope for configuring NBT components with interpretation, separators, style, and children.
 */
@KotventureDslMarker
public interface NbtScope : ComponentScope {
    /**
     * Sets whether fetched NBT should be parsed as component JSON.
     */
    public fun interpret(interpret: Boolean)

    /**
     * Applies [separator] between multiple NBT values.
     */
    public fun separator(separator: ComponentLike)

    /**
     * Builds and applies an inline text separator between multiple NBT values.
     */
    public fun separator(init: TextScope.() -> Unit)
}
