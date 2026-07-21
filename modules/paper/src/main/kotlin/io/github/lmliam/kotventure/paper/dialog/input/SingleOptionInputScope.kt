package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures a single-choice dialog input.
 *
 * You must set [label] and add at least one option. Option identifiers must be unique, and only one
 * option can be the default.
 */
@KotventureDslMarker
public interface SingleOptionInputScope {
    /**
     * Builds the required input label and configures its visibility.
     *
     * @throws IllegalStateException when the label is already set in this block.
     */
    public fun label(init: LabelScope.() -> Unit): Unit

    /**
     * Sets the required input label.
     *
     * Paper uses its default visibility. Use the block overload to set the visibility.
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
     * Adds options configured by [init].
     *
     * Multiple calls add options in call order. Validation occurs after the input block completes.
     *
     * The completed input fails if it has no options, duplicate identifiers, or multiple default
     * options.
     */
    public fun options(init: OptionsScope.() -> Unit): Unit
}
