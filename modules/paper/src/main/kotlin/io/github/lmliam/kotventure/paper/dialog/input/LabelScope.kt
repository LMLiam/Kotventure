package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.ComponentScope

/**
 * Builds an input label and controls its visibility.
 *
 * Use the inherited [ComponentScope] functions to build the label component. If you do not call
 * [visible], Paper uses its default visibility.
 */
public interface LabelScope : ComponentScope {
    /**
     * Sets whether the input shows the label.
     *
     * The default argument is `true`.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun visible(value: Boolean = true): Unit
}
