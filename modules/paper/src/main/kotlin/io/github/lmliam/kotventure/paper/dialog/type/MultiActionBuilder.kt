@file:Suppress("UnstableApiUsage")

package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.core.dsl.positive
import io.github.lmliam.kotventure.paper.dialog.DialogBaseBuilder
import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonBuilder
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.type.DialogType

internal class MultiActionBuilder(
    private val base: DialogBaseBuilder,
) : MultiActionScope,
    DialogScope by base {
    private val actions = mutableListOf<ActionButton>()
    private var columns: Int? by once().positive()
    private var exit: ActionButton? by once { "'exitButton' is already set." }

    override fun button(init: ButtonScope.() -> Unit) {
        actions += ButtonBuilder().apply(init).build()
    }

    override fun columns(value: Int) {
        columns = value
    }

    override fun exitButton(init: ButtonScope.() -> Unit) {
        exit = ButtonBuilder().apply(init).build()
    }

    internal fun build(): Dialog {
        check(actions.isNotEmpty()) { "multiAction requires at least one 'button'." }

        val type =
            DialogType
                .multiAction(actions)
                .apply {
                    columns?.let(::columns)
                    exit?.let(::exitAction)
                }.build()
        return base.build(type)
    }
}
