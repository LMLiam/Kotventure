package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Configures the command-template values for a Boolean input.
 *
 * Use `true("yes")` for the checked state. Use `false("no")` for the clear state. You can set each
 * state one time.
 */
@KotventureDslMarker
public interface BooleanValuesScope {
    /**
     * Sets the command-template value for the receiver state.
     *
     * @throws IllegalStateException when this state's substitution is already set in this block.
     */
    public operator fun Boolean.invoke(value: String): Unit
}
