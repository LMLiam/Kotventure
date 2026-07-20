package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures a single-option (radio) dialog input. The label and at least one option are required.
 */
@KotventureDslMarker
public interface SingleOptionInputScope {
    /**
     * Builds the required input label — text and visibility — from a [LabelScope] block.
     *
     * @throws IllegalStateException when the label is already set in this block.
     */
    public fun label(init: LabelScope.() -> Unit): Unit

    /**
     * Sets the required input label. Visibility uses Paper's default. Use the block overload to set visibility.
     *
     * @throws IllegalStateException when the label is already set in this block.
     */
    public fun <T : ComponentLike> label(component: T): Unit

    /**
     * Sets the input width in pixels (1..1024).
     *
     * @throws IllegalStateException when this slot is already set in this block.
     * @throws IllegalArgumentException when [value] is outside 1..1024.
     */
    public fun width(value: Int): Unit

    /**
     * Declares the input's options with [init]. Options accumulate in order.
     *
     * @throws IllegalStateException when no option is declared, an id repeats, or more than one
     *   option is marked default.
     */
    public fun options(init: OptionsScope.() -> Unit): Unit
}
