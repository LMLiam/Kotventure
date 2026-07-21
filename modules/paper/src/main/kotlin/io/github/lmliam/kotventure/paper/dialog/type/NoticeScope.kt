package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope

/**
 * Configures a notice dialog.
 *
 * In addition to the required [DialogScope.title], you can configure one acknowledgement
 * [button]. Paper supplies the button when you do not configure it.
 */
public interface NoticeScope : DialogScope {
    /**
     * Configures the optional acknowledgement button.
     *
     * @throws IllegalStateException when the button is already configured in this block.
     * @throws IllegalStateException when [init] does not set the required button label.
     */
    public fun button(init: ButtonScope.() -> Unit): Unit
}
