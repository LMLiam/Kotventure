package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures a boolean dialog input. The label is required; every other slot is optional.
 */
@KotventureDslMarker
public interface BooleanInputScope {
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
     * Sets the default checked state. Called with no argument, sets it to `true`.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun default(value: Boolean = true): Unit

    /**
     * Sets the strings substituted into command templates for each state, configured by [init].
     *
     * @throws IllegalStateException when the values are already configured in this block.
     */
    public fun values(init: BooleanValuesScope.() -> Unit): Unit
}
