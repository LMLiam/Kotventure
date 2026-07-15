package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.set.RegistrySet

/**
 * Configures a dialog-list dialog. Beyond the base [DialogScope] slots, the presented [dialogs]
 * are required; every other slot here is optional.
 */
public interface DialogListScope : DialogScope {
    /**
     * Sets the required set of dialogs presented as entry buttons.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun dialogs(dialogs: RegistrySet<Dialog>): Unit

    /**
     * Sets the number of columns the entries are laid out in.
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
     */
    public fun exitButton(init: ButtonScope.() -> Unit): Unit
}
