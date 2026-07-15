package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Configures the strings substituted into command templates for each state of a boolean input.
 * `true("yes")` sets the substitution used when the input is on; `false("no")` when it is off.
 */
@KotventureDslMarker
public interface BooleanValuesScope {
    /**
     * Sets the substitution for this boolean state (`true` for on, `false` for off).
     *
     * @throws IllegalStateException when this state's substitution is already set in this block.
     */
    public operator fun Boolean.invoke(value: String): Unit
}
