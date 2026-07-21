package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope

/**
 * Configures a dialog that shows the server link list.
 *
 * In addition to the required [DialogScope.title], you must set [columns] and [buttonWidth]. The
 * exit button is optional.
 */
public interface ServerLinksScope : DialogScope {
    /**
     * Sets the required number of columns that contain the links.
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
     * @throws IllegalStateException when [init] does not set the button label.
     */
    public fun exitButton(init: ButtonScope.() -> Unit): Unit
}
