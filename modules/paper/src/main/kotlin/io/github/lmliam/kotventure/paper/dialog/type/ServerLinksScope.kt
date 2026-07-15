package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope

/**
 * Configures a server-links dialog. Beyond the base [DialogScope] slots, the [columns] and
 * [buttonWidth] slots are required; only the exit button is optional.
 */
public interface ServerLinksScope : DialogScope {
    /**
     * Sets the required number of columns the links are laid out in.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     * @throws IllegalArgumentException when [value] is not positive.
     */
    public fun columns(value: Int): Unit

    /**
     * Sets the required width of each link button in pixels (1..1024).
     *
     * @throws IllegalStateException when this slot is already set in this block.
     * @throws IllegalArgumentException when [value] is outside 1..1024.
     */
    public fun buttonWidth(value: Int): Unit

    /**
     * Configures the optional exit button.
     *
     * @throws IllegalStateException when the exit button is already configured in this block.
     */
    public fun exitButton(init: ButtonScope.() -> Unit): Unit
}
