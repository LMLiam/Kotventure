package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope

/**
 * Configures a notice dialog. Beyond the base [DialogScope] slots, its single acknowledgement
 * [button] is optional; left unset, Paper supplies the default button.
 */
public interface NoticeScope : DialogScope {
    /**
     * Configures the optional acknowledgement button.
     *
     * @throws IllegalStateException when the button is already configured in this block, or when
     *   [init] does not set the required button label.
     */
    public fun button(init: ButtonScope.() -> Unit): Unit
}
