@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog

import io.github.lmliam.kotventure.core.component.ComponentScope
import io.github.lmliam.kotventure.core.component.component
import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.paper.dialog.body.ItemBodyBuilder
import io.github.lmliam.kotventure.paper.dialog.body.ItemBodyScope
import io.github.lmliam.kotventure.paper.dialog.input.InputsBuilder
import io.github.lmliam.kotventure.paper.dialog.input.InputsScope
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.InlinedRegistryBuilderProvider
import io.papermc.paper.registry.data.dialog.DialogBase
import io.papermc.paper.registry.data.dialog.DialogBase.DialogAfterAction
import io.papermc.paper.registry.data.dialog.body.DialogBody
import io.papermc.paper.registry.data.dialog.input.DialogInput
import io.papermc.paper.registry.data.dialog.type.DialogType
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import org.bukkit.inventory.ItemStack

internal class DialogBaseBuilder : DialogScope {
    private var title: Component? by once()
    private var externalTitle: Component? by once()
    private var closeOnEscape: Boolean? by once()
    private var pausesGame: Boolean? by once()
    private var afterAction: DialogAfterAction? by once { "'afterAction' is already set." }
    private val bodies = mutableListOf<DialogBody>()
    private val inputs = mutableListOf<DialogInput>()

    override val close: DialogAfterAction get() = DialogAfterAction.CLOSE
    override val none: DialogAfterAction get() = DialogAfterAction.NONE
    override val wait: DialogAfterAction get() = DialogAfterAction.WAIT_FOR_RESPONSE

    override fun title(init: ComponentScope.() -> Unit) = title(component(init))

    override fun <T : ComponentLike> title(component: T) {
        title = component.asComponent()
    }

    override fun externalTitle(init: ComponentScope.() -> Unit) = externalTitle(component(init))

    override fun <T : ComponentLike> externalTitle(component: T) {
        externalTitle = component.asComponent()
    }

    override fun closeOnEscape(value: Boolean) {
        closeOnEscape = value
    }

    override fun pausesGame(value: Boolean) {
        pausesGame = value
    }

    override fun afterAction(action: DialogAfterAction) {
        afterAction = action
    }

    override fun message(init: ComponentScope.() -> Unit) = message(component(init))

    override fun <T : ComponentLike> message(component: T) {
        bodies += DialogBody.plainMessage(component.asComponent())
    }

    override fun item(
        stack: ItemStack,
        init: ItemBodyScope.() -> Unit,
    ) {
        bodies += ItemBodyBuilder(stack).apply(init).build()
    }

    override fun item(stack: ItemStack) {
        bodies += ItemBodyBuilder(stack).build()
    }

    override fun inputs(init: InputsScope.() -> Unit) {
        inputs += InputsBuilder().apply(init).build()
    }

    private fun buildBase(): DialogBase {
        val dialogTitle = checkNotNull(title) { "a dialog requires a 'title' slot." }

        return DialogBase
            .builder(dialogTitle)
            .apply {
                externalTitle?.let(::externalTitle)
                canCloseWithEscape(closeOnEscape ?: true)
                pause(pausesGame ?: true)
                afterAction(afterAction ?: DialogAfterAction.CLOSE)
                body(bodies)
                inputs(inputs)
            }.build()
    }

    internal fun build(type: DialogType): Dialog =
        InlinedRegistryBuilderProvider.instance().createDialog { factory ->
            factory.empty().base(buildBase()).type(type)
        }
}
