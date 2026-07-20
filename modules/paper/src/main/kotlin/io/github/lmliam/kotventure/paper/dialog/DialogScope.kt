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
 * Configures the common [base][io.papermc.paper.registry.data.dialog.DialogBase] of a [Dialog]:
 * its title, framing, body, and inputs. The dialog type is chosen at the call site with a
 * [DialogKind] token, so each kind's scope extends this base with only its own capabilities.
 *
 * The [title] slot is required. Each other base slot is optional. Body and [inputs] calls are
 * repeatable and accumulate in call order.
 *
 * @sample io.github.lmliam.kotventure.paper.dialog.dialogSample
 */
@KotventureDslMarker
public interface DialogScope {
    /**
     * The after-action that returns to the previous non-dialog screen when the dialog closes.
     */
    public val close: DialogAfterAction

    /**
     * The after-action that keeps the current screen open when the dialog closes.
     */
    public val none: DialogAfterAction

    /**
     * The after-action that shows a "waiting for response" screen while the dialog is handled.
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
     * Sets whether the dialog can be dismissed with the escape keybind. Called with no argument,
     * sets it to `true` (defaults to `true` when unset).
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun closeOnEscape(value: Boolean = true): Unit

    /**
     * Sets whether the dialog pauses a single-player game while it is open. Called with no
     * argument, sets it to `true` (defaults to `true` when unset).
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun pausesGame(value: Boolean = true): Unit

    /**
     * Sets the action taken after the dialog is closed. Use [close], [none], or [wait].
     * Defaults to [close] when unset.
     *
     * @throws IllegalStateException when this slot is already set in this block.
     */
    public fun afterAction(action: DialogAfterAction): Unit

    /**
     * Appends a plain-message body from a component block. Calls accumulate in order.
     */
    public fun message(init: ComponentScope.() -> Unit): Unit

    /**
     * Appends a plain-message body. Calls accumulate in order.
     */
    public fun <T : ComponentLike> message(component: T): Unit

    /**
     * Appends an item body for [stack] with [init]. Calls accumulate in order.
     */
    public fun item(
        stack: ItemStack,
        init: ItemBodyScope.() -> Unit,
    ): Unit

    /**
     * Appends an item body for [stack] with default framing. Calls accumulate in order.
     */
    public fun item(stack: ItemStack): Unit

    /**
     * Declares dialog inputs with [init]. Declarations accumulate in call order across all blocks.
     */
    public fun inputs(init: InputsScope.() -> Unit): Unit
}
