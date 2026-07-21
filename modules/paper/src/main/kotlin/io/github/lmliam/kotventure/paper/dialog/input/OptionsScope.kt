package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Adds entries to a single-choice input.
 *
 * The receiver string is the entry identifier. Calls add entries in call order. The completed
 * input rejects duplicate identifiers.
 */
@KotventureDslMarker
public interface OptionsScope {
    /**
     * Adds an entry with the receiver identifier and configures it with [init].
     */
    public operator fun String.invoke(init: OptionScope.() -> Unit): Unit

    /**
     * Adds an entry with the receiver identifier and Paper's default display.
     */
    public operator fun String.unaryPlus(): Unit
}
