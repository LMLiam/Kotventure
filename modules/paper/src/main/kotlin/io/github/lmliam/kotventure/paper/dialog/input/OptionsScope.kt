package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Declares the options of a single-option input. Each option is identified by its id and
 * accumulates in call order.
 */
@KotventureDslMarker
public interface OptionsScope {
    /**
     * Adds an option with this id, configured by [init].
     */
    public operator fun String.invoke(init: OptionScope.() -> Unit): Unit

    /**
     * Adds an option with this id and no display override.
     */
    public operator fun String.unaryPlus(): Unit
}
