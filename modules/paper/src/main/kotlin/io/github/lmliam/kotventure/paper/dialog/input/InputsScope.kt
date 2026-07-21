package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Adds inputs to a dialog.
 *
 * Each declaration supplies the key that identifies its value in a dialog action. Use a unique
 * key for each input. This scope preserves declaration order but does not check key uniqueness.
 */
@KotventureDslMarker
public interface InputsScope {
    /**
     * Adds a text input with [key] and configures it with [init].
     *
     * @throws IllegalStateException when [init] does not set the required label.
     * @throws IllegalArgumentException when [init] sets a value outside its permitted range.
     */
    public fun text(
        key: String,
        init: TextInputScope.() -> Unit,
    ): Unit

    /**
     * Adds a Boolean input with [key] and configures it with [init].
     *
     * @throws IllegalStateException when [init] does not set the required label.
     */
    public fun boolean(
        key: String,
        init: BooleanInputScope.() -> Unit,
    ): Unit

    /**
     * Adds a number-range input with [key] and the inclusive [range].
     *
     * @throws IllegalStateException when [init] does not set the required label.
     * @throws IllegalArgumentException when [init] sets a value outside its permitted range.
     */
    public fun range(
        key: String,
        range: ClosedFloatingPointRange<Float>,
        init: NumberRangeInputScope.() -> Unit,
    ): Unit

    /**
     * Adds a single-choice input with [key] and configures it with [init].
     *
     * @throws IllegalStateException when the label or options are incomplete, option identifiers
     *   repeat, or more than one option is the default.
     * @throws IllegalArgumentException when [init] sets a value outside its permitted range.
     */
    public fun option(
        key: String,
        init: SingleOptionInputScope.() -> Unit,
    ): Unit
}
