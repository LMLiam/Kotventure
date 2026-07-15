@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.core.dsl.inRange
import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.dsl.positive
import io.github.lmliam.kotventure.paper.dialog.DialogBaseBuilder
import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonBuilder
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.type.DialogType
import io.papermc.paper.registry.set.RegistrySet

internal class DialogListBuilder(
    private val base: DialogBaseBuilder,
) : DialogListScope,
    DialogScope by base {
    private var dialogs: RegistrySet<Dialog>? by once { "'dialogs' is already set." }
    private var columns: Int? by once().positive()
    private var buttonWidth: Int? by once().inRange(1..1024)
    private var exit: ActionButton? by once { "'exitButton' is already set." }

    override fun dialogs(dialogs: RegistrySet<Dialog>) {
        this.dialogs = dialogs
    }

    override fun columns(value: Int) {
        columns = value
    }

    override fun buttonWidth(value: Int) {
        buttonWidth = value
    }

    override fun exitButton(init: ButtonScope.() -> Unit) {
        exit = ButtonBuilder().apply(init).build()
    }

    internal fun build(): Dialog {
        val entries = checkNotNull(dialogs) { "a dialog list requires a 'dialogs' slot." }
        val type =
            DialogType
                .dialogList(entries)
                .apply {
                    columns?.let(::columns)
                    buttonWidth?.let(::buttonWidth)
                    exit?.let(::exitAction)
                }.build()
        return base.build(type)
    }
}
