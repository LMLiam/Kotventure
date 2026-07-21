package io.github.lmliam.kotventure.paper.dialog

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.paper.dialog.body.ItemBodyScope
import io.github.lmliam.kotventure.paper.dialog.input.InputsScope
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.DialogBase.DialogAfterAction
import net.kyori.adventure.text.ComponentLike
import org.bukkit.inventory.ItemStack

/**
 * Configures the common content and behaviour of a [Dialog].
 *
 * You must set [title] one time. Each other single-value setting is optional and can be set one
 * time. Calls to [message], [item], and [inputs] add values in call order.
 *
 * @sample io.github.lmliam.kotventure.paper.dialog.dialogSample
 */
@KotventureDslMarker
public interface DialogScope {
    /**
     * Returns the action that opens the previous non-dialog screen after the dialog closes.
     */
    public val close: DialogAfterAction

    /**
     * Returns the action that does not change the current screen after the dialog closes.
     */
    public val none: DialogAfterAction

    /**
     * Returns the action that shows a wait screen while the server processes the response.
     */
    public val wait: DialogAfterAction

    /**
     * Builds the required dialog title from a component block.
     *
     * @throws IllegalStateException when the title is already set in this block.
     */
    public fun title(init: ComponentScope.() -> Unit): Unit

    /**
     * Sets the required dialog title.
     *
     * @throws IllegalStateException when the title is already set in this block.
     */
    public fun <T : ComponentLike> title(component: T): Unit

    /**
     * Builds the optional external title (shown on buttons that open this dialog) from a block.
     *
     * @throws IllegalStateException when the external title is already set in this block.
     */
    public fun externalTitle(init: ComponentScope.() -> Unit): Unit

    /**
     * Sets the optional external title, shown on buttons that open this dialog.
     *
     * @throws IllegalStateException when the external title is already set in this block.
     */
    public fun <T : ComponentLike> externalTitle(component: T): Unit

    /**
     * Sets whether the user can close the dialog with the escape key.
     *
     * The argument and the Paper default are `true`.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun closeOnEscape(value: Boolean = true): Unit

    /**
     * Sets whether the dialog pauses a single-player game while it is open.
     *
     * The argument and the Paper default are `true`.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun pausesGame(value: Boolean = true): Unit

    /**
     * Sets the action that occurs after the dialog closes.
     *
     * Use [close], [none], or [wait]. The default is [close].
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun afterAction(action: DialogAfterAction): Unit

    /**
     * Adds a plain-message body from a component block.
     *
     * Multiple calls add bodies in call order.
     */
    public fun message(init: ComponentScope.() -> Unit): Unit

    /**
     * Adds a plain-message body.
     *
     * Multiple calls add bodies in call order.
     */
    public fun <T : ComponentLike> message(component: T): Unit

    /**
     * Adds an item body for [stack] and configures its presentation with [init].
     *
     * Multiple calls add bodies in call order. This function does not modify [stack].
     */
    public fun item(
        stack: ItemStack,
        init: ItemBodyScope.() -> Unit,
    ): Unit

    /**
     * Adds an item body for [stack] with Paper's default presentation.
     *
     * Multiple calls add bodies in call order. This function does not modify [stack].
     */
    public fun item(stack: ItemStack): Unit

    /**
     * Adds dialog inputs configured by [init].
     *
     * Multiple calls add inputs in call order.
     */
    public fun inputs(init: InputsScope.() -> Unit): Unit
}
