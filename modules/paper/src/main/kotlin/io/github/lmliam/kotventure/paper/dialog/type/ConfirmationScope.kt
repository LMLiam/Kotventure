package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope

/**
 * Configures a confirmation dialog. Beyond the base [DialogScope] slots, both the [yes] and [no]
 * buttons are required.
 */
public interface ConfirmationScope : DialogScope {
    /**
     * Configures the required confirming button.
     *
     * @throws IllegalStateException when the yes button is already configured in this block.
     */
    public fun yes(init: ButtonScope.() -> Unit): Unit

    /**
     * Configures the required denying button.
     *
     * @throws IllegalStateException when the no button is already configured in this block.
     */
    public fun no(init: ButtonScope.() -> Unit): Unit
}
