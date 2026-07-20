package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures a number-range dialog input. The label is required. Each other slot is optional.
 * The range itself is supplied when the input is declared.
 */
@KotventureDslMarker
public interface NumberRangeInputScope {
    /**
     * The `%1$s` placeholder — the input's label — for use in [format].
     */
    public val label: String

    /**
     * The `%2$s` placeholder — the current value — for use in [format].
     */
    public val value: String

    /**
     * Builds the required input label from a component block.
     *
     * @throws IllegalStateException when the label is already set in this block.
     */
    public fun label(init: ComponentScope.() -> Unit): Unit

    /**
     * Sets the required input label.
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
     * Sets the label-format string by joining [parts]. Combine the [label] and [value] placeholder
     * tokens with literal text — `format(label, ": ", value)` yields `"%1$s: %2$s"`. A single
     * non-token part doubles as a raw translation-key bridge, e.g.
     * `format("options.generic_value")`.
     *
     * @throws IllegalStateException when the format is already set in this block.
     * @throws IllegalArgumentException when called with no parts.
     */
    public fun format(vararg parts: String): Unit

    /**
     * Sets the default value on the range.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun default(value: Float): Unit

    /**
     * Sets the step size between selectable values.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     * @throws IllegalArgumentException when [value] is not positive.
     */
    public fun step(value: Float): Unit
}
