package io.github.lmliam.kotventure.paper.dialog.input

import io.github.lmliam.kotventure.core.component.ComponentScope

/**
 * Configures an input label: a component block (via the inherited [ComponentScope]) plus whether
 * the label is shown.
 */
public interface LabelScope : ComponentScope {
    /**
     * Sets whether the label is shown. Called with no argument, sets it to `true`.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun visible(value: Boolean = true): Unit
}
