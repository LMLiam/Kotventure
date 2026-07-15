package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures a text dialog input. The label is required; every other slot is optional.
 */
@KotventureDslMarker
public interface TextInputScope {
    /**
     * Builds the required input label — text and visibility — from a [LabelScope] block.
     *
     * @throws IllegalStateException when the label is already set in this block.
     */
    public fun label(init: LabelScope.() -> Unit): Unit

    /**
     * Sets the required input label. Visibility follows Paper's default; use the block overload to
     * set it.
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
     * Sets the default text value.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun default(value: String): Unit

    /**
     * Sets the maximum input length.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     * @throws IllegalArgumentException when [value] is not positive.
     */
    public fun maxLength(value: Int): Unit

    /**
     * Enables multi-line input configured by [init].
     *
     * @throws IllegalStateException when multiline is already configured in this block.
     */
    public fun multiline(init: TextMultilineScope.() -> Unit): Unit
}
