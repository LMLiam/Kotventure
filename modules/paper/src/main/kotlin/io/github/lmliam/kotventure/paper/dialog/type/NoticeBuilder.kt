package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.paper.dialog.DialogBaseBuilder
import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonBuilder
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.type.DialogType

internal class NoticeBuilder(
    private val base: DialogBaseBuilder,
) : NoticeScope,
    DialogScope by base {
    private var button: ActionButton? by once { "the notice button is already set." }

    override fun button(init: ButtonScope.() -> Unit) {
        button = ButtonBuilder().apply(init).build()
    }

    internal fun build(): Dialog {
        val type = button?.let(DialogType::notice) ?: DialogType.notice()
        return base.build(type)
    }
}
