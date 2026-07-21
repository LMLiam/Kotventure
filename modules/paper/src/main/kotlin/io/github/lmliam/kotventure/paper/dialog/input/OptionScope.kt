package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import net.kyori.adventure.text.ComponentLike

/**
 * Configures one entry in a single-choice input.
 *
 * Both settings are optional. The input can have only one default entry.
 */
@KotventureDslMarker
public interface OptionScope {
    /**
     * Builds the optional display label from a component block.
     *
     * @throws IllegalStateException when the display is already set in this block.
     */
    public fun display(init: ComponentScope.() -> Unit): Unit

    /**
     * Sets the optional display label.
     *
     * @throws IllegalStateException when the display is already set in this block.
     */
    public fun <T : ComponentLike> display(component: T): Unit

    /**
     * Marks this option as the initially selected entry.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun default(): Unit
}
