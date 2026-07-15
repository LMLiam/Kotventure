package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker

/**
 * Configures multi-line options for a text input. Both slots are optional.
 */
@KotventureDslMarker
public interface TextMultilineScope {
    /**
     * Sets the maximum number of lines.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     * @throws IllegalArgumentException when [value] is not positive.
     */
    public fun maxLines(value: Int): Unit

    /**
     * Sets the input height in pixels (1..512).
     *
     * @throws IllegalStateException when this slot is already set in this block.
     * @throws IllegalArgumentException when [value] is outside 1..512.
     */
    public fun height(value: Int): Unit
}
