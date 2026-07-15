package io.github.lmliam.kotventure.paper.dialog.type

import io.github.lmliam.kotventure.core.dsl.once
import io.github.lmliam.kotventure.paper.dialog.DialogBaseBuilder
import io.github.lmliam.kotventure.paper.dialog.DialogScope
import io.github.lmliam.kotventure.paper.dialog.action.ButtonBuilder
import io.github.lmliam.kotventure.paper.dialog.action.ButtonScope
import io.papermc.paper.dialog.Dialog
import io.papermc.paper.registry.data.dialog.ActionButton
import io.papermc.paper.registry.data.dialog.type.DialogType

internal class ConfirmationBuilder(
    private val base: DialogBaseBuilder,
) : ConfirmationScope,
    DialogScope by base {
    private var yes: ActionButton? by once { "'yes' is already set." }
    private var no: ActionButton? by once { "'no' is already set." }

    override fun yes(init: ButtonScope.() -> Unit) {
        yes = ButtonBuilder().apply(init).build()
    }

    override fun no(init: ButtonScope.() -> Unit) {
        no = ButtonBuilder().apply(init).build()
    }

    internal fun build(): Dialog {
        val type =
            DialogType.confirmation(
                checkNotNull(yes) { "confirmation requires a 'yes' button." },
                checkNotNull(no) { "confirmation requires a 'no' button." },
            )
        return base.build(type)
    }
}
