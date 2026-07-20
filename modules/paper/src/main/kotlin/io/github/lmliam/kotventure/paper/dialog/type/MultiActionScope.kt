package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope

/**
 * Configures a multi-action dialog. Beyond the base [DialogScope] slots, at least one [button] is
 * required. The column count and exit button are optional.
 */
public interface MultiActionScope : DialogScope {
    /**
     * Adds an action button with [init]. Calls accumulate in order.
     */
    public fun button(init: ButtonScope.() -> Unit): Unit

    /**
     * Sets the number of columns the buttons are laid out in.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     * @throws IllegalArgumentException when [value] is not positive.
     */
    public fun columns(value: Int): Unit

    /**
     * Configures the optional exit button.
     *
     * @throws IllegalStateException when the exit button is already configured in this block.
     */
    public fun exitButton(init: ButtonScope.() -> Unit): Unit
}
