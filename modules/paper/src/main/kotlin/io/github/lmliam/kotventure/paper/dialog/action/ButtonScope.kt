package io.github.lmliam.kotventure.paper.dialog.action

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.dsl.KotventureDslMarker
import io.github.lmliam.kotventure.core.event.ClickActionScope
import io.papermc.paper.registry.data.dialog.action.DialogActionCallback
import net.kyori.adventure.key.Key
import net.kyori.adventure.nbt.api.BinaryTagHolder
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.ClickCallback
import kotlin.time.Duration

/**
 * Configures an action button.
 *
 * You must set [label]. The tooltip, width, and action are optional. Select zero or one of
 * [onClick], [runCommand], [custom], and [click]. A second action fails immediately.
 */
@KotventureDslMarker
public interface ButtonScope {
    /**
     * Builds the required button label from a component block.
     *
     * @throws IllegalStateException when the label is already set in this block.
     */
    public fun label(init: ComponentScope.() -> Unit): Unit

    /**
     * Sets the required button label.
     *
     * @throws IllegalStateException when the label is already set in this block.
     */
    public fun <T : ComponentLike> label(component: T): Unit

    /**
     * Builds the optional hover tooltip from a component block.
     *
     * @throws IllegalStateException when the tooltip is already set in this block.
     */
    public fun tooltip(init: ComponentScope.() -> Unit): Unit

    /**
     * Sets the optional hover tooltip.
     *
     * @throws IllegalStateException when the tooltip is already set in this block.
     */
    public fun <T : ComponentLike> tooltip(component: T): Unit

    /**
     * Sets the button width in pixels (1..1024).
     *
     * @throws IllegalStateException when the width is already set in this block.
     * @throws IllegalArgumentException when [value] is outside 1..1024.
     */
    public fun width(value: Int): Unit

    /**
     * Selects a server-side callback action.
     *
     * Paper calls [callback] with the response and the audience that selected the button.
     *
     * @throws IllegalStateException when an action is already selected in this block.
     */
    public fun onClick(callback: DialogActionCallback): Unit

    /**
     * Selects a server-side callback action with a use limit and a lifetime.
 *
     * @throws IllegalStateException when an action is already selected in this block.
     * @throws IllegalArgumentException when [uses] or [lifetime] is not valid for Paper.
     */
    public fun onClick(
        uses: Int,
        lifetime: Duration,
        callback: DialogActionCallback,
    ): Unit

    /**
     * Selects a server-side callback action with prebuilt [options].
     *
     * @throws IllegalStateException when an action is already selected in this block.
     */
    public fun onClick(
        options: ClickCallback.Options,
        callback: DialogActionCallback,
    ): Unit

    /**
     * Selects a command-template action.
     *
     * At run time, Paper replaces each `$(input_key)` placeholder with its input value.
     *
     * @throws IllegalStateException when an action is already selected in this block.
     */
    public fun runCommand(template: String): Unit

    /**
     * Selects a custom client action identified by [id].
     *
     * @throws IllegalStateException when an action is already selected in this block.
     */
    public fun custom(id: Key): Unit

    /**
     * Selects a custom client action identified by [id] carrying [additions].
     *
     * @throws IllegalStateException when an action is already selected in this block.
     */
    public fun custom(
        id: Key,
        additions: BinaryTagHolder,
    ): Unit

    /**
     * Selects a static click-event action with the core click DSL.
     *
     * @throws IllegalStateException when an action is already selected in this block, or when
     *   [init] does not choose exactly one click action.
     * @throws IllegalArgumentException when the selected click action has an invalid value.
     */
    public fun click(init: ClickActionScope.() -> Unit): Unit
}
