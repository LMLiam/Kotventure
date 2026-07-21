package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.set.RegistrySet

/**
 * Configures a dialog that links to other dialogs.
 *
 * In addition to the required [DialogScope.title], you must set [dialogs]. Each other setting is
 * optional.
 */
public interface DialogListScope : DialogScope {
    /**
     * Sets the required set of dialogs presented as entry buttons.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun dialogs(dialogs: RegistrySet<Dialog>): Unit

    /**
     * Sets the number of columns that contain the entries.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     * @throws IllegalArgumentException when [value] is not positive.
     */
    public fun columns(value: Int): Unit

    /**
     * Sets the width of each entry button in pixels (1..1024).
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
