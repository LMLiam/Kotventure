package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Declares a dialog's inputs. Each input is identified by a unique key and accumulates in call
 * order; grouping inputs here is independent of the dialog type.
 */
@KotventureDslMarker
public interface InputsScope {
    /**
     * Declares a text input identified by [key], configured by [init].
     *
     * @throws IllegalStateException when [init] does not set the required label.
     */
    public fun text(
        key: String,
        init: TextInputScope.() -> Unit,
    ): Unit

    /**
     * Declares a boolean input identified by [key], configured by [init].
     *
     * @throws IllegalStateException when [init] does not set the required label.
     */
    public fun boolean(
        key: String,
        init: BooleanInputScope.() -> Unit,
    ): Unit

    /**
     * Declares a number-range (slider) input identified by [key] over [range], configured by
     * [init].
     *
     * @throws IllegalStateException when [init] does not set the required label.
     */
    public fun range(
        key: String,
        range: ClosedFloatingPointRange<Float>,
        init: NumberRangeInputScope.() -> Unit,
    ): Unit

    /**
     * Declares a single-option (radio) input identified by [key], configured by [init].
     *
     * @throws IllegalStateException when [init] does not set the required label or valid options.
     */
    public fun option(
        key: String,
        init: SingleOptionInputScope.() -> Unit,
    ): Unit
}
