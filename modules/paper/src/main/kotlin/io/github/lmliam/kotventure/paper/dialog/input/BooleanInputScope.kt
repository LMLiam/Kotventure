package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures a Boolean dialog input.
 *
 * You must set [label]. Each other setting is optional and can be set one time.
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
     * Sets the initial checked state.
     *
     * The default argument is `true`.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun default(value: Boolean = true): Unit

    /**
     * Sets the command-template values for the checked and clear states.
     *
     * Each state is optional. Paper supplies its default value for a state that [init] does not set.
     *
     * @throws IllegalStateException when the values are already configured in this block.
     */
    public fun values(init: BooleanValuesScope.() -> Unit): Unit
}
