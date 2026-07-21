package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope

/**
 * Configures a confirmation dialog.
 *
 * In addition to the required [DialogScope.title], you must configure [yes] and [no].
 */
public interface ConfirmationScope : DialogScope {
    /**
     * Configures the required confirming button.
     *
     * @throws IllegalStateException when the yes button is already configured in this block.
     * @throws IllegalStateException when [init] does not set the button label.
     */
    public fun yes(init: ButtonScope.() -> Unit): Unit

    /**
     * Configures the required denying button.
     *
     * @throws IllegalStateException when the no button is already configured in this block.
     * @throws IllegalStateException when [init] does not set the button label.
     */
    public fun no(init: ButtonScope.() -> Unit): Unit
}
