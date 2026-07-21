package io.github.lmliam.kotventure.paper.dialog.body

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures the presentation of an item in a dialog body.
 *
 * Each setting is optional and can be set one time. An unset setting uses Paper's default.
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
     * Sets whether the body shows item decorations, such as the damage bar and stack count.
     *
     * The default argument is `true`.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun decorations(value: Boolean = true): Unit

    /**
     * Sets whether the body shows the item tooltip when the user points at the item.
     *
     * The default argument is `true`.
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
