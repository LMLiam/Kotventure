package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures a number-range dialog input.
 *
 * You must set the input label. The declaration supplies the range. Each other setting is
 * optional and can be set one time.
 */
@KotventureDslMarker
public interface NumberRangeInputScope {
    /**
     * Returns the literal `%1$s` placeholder for the input label in [format].
     */
    public val label: String

    /**
     * Returns the literal `%2$s` placeholder for the current input value in [format].
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
     * Sets the label format to the concatenation of [parts].
     *
     * Use [label] and [value] as substitution placeholders. For example,
     * `format(label, ": ", value)` produces `"%1$s: %2$s"`. You can also supply a translation key
     * such as `format("options.generic_value")`.
     *
     * @throws IllegalStateException when the format is already set in this block.
     * @throws IllegalArgumentException when called with no parts.
     */
    public fun format(vararg parts: String): Unit

    /**
     * Sets the initially selected value.
     *
     * This DSL passes [value] to Paper. It does not check that the value is in the declared range.
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
