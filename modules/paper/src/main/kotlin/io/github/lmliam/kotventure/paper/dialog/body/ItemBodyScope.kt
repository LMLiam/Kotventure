package io.github.lmliam.kotventure.paper.dialog.body

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures an item dialog body. Each slot is optional. An unset slot uses Paper's default.
 */
@KotventureDslMarker
public interface ItemBodyScope {
    /**
     * Builds the optional description shown beside the item from a component block.
     *
     * @throws IllegalStateException when the description is already set in this block.
     */
    public fun description(init: ComponentScope.() -> Unit): Unit

    /**
     * Sets the optional description shown beside the item.
     *
     * @throws IllegalStateException when the description is already set in this block.
     */
    public fun <T : ComponentLike> description(component: T): Unit

    /**
     * Sets whether item decorations (damage bar, stack count) are shown. Called with no argument,
     * sets it to `true`.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun decorations(value: Boolean = true): Unit

    /**
     * Sets whether the item's tooltip is shown on hover. Called with no argument, sets it to
     * `true`.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun tooltip(value: Boolean = true): Unit

    /**
     * Sets the body width in pixels (1..256).
     *
     * @throws IllegalStateException when this slot is already set in this block.
     * @throws IllegalArgumentException when [value] is outside 1..256.
     */
    public fun width(value: Int): Unit

    /**
     * Sets the body height in pixels (1..256).
     *
     * @throws IllegalStateException when this slot is already set in this block.
     * @throws IllegalArgumentException when [value] is outside 1..256.
     */
    public fun height(value: Int): Unit
}
